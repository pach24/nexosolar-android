package com.nexosolar.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.Logger
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.maxAmount
import com.nexosolar.android.domain.models.minAmount
import com.nexosolar.android.domain.models.newestDate
import com.nexosolar.android.domain.models.oldestDate
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.RefreshInvoicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel reactivo para la gestión de facturas usando StateFlow.
 *
 * MIGRACIÓN A STATEFLOW:
 * - Reemplaza LiveData con StateFlow (estándar moderno).
 * - Consume el Flow del UseCase de forma reactiva.
 * - Centraliza el estado en InvoiceUiState (sealed interface).
 */

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase,
    private val refreshInvoicesUseCase: RefreshInvoicesUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "InvoiceVM"
    }


    // ========== STATEFLOWS (Fuente de verdad) ==========

    // Estado principal de la UI (Carga, Éxito, Error)
    private val _uiState = MutableStateFlow<InvoiceUIState>(InvoiceUIState.Loading)
    val uiState: StateFlow<InvoiceUIState> = _uiState.asStateFlow()

    // Estado de filtros (separado porque es independiente del estado de carga de la lista)
    private val _filterState = MutableStateFlow(InvoiceFilterUIState())
    val filterState: StateFlow<InvoiceFilterUIState> = _filterState.asStateFlow()

    // Copia local para filtrado rápido sin volver a BD
    private var originalInvoices: List<Invoice> = emptyList()

    // Job de recolección para poder cancelarlo/reiniciarlo si fuera necesario
    private var collectionJob: Job? = null

    init {
        observarFacturas()
    }

    /**
     * Se suscribe al Flow de facturas del dominio.
     * Cualquier cambio en Room actualizará automáticamente la UI.
     */
    private fun observarFacturas() {
        // Cancelamos cualquier job previo para evitar fugas o múltiples suscripciones
        collectionJob?.cancel()

        collectionJob = viewModelScope.launch {
            // OJO: No pongas _uiState.value = Loading aquí si quieres que el SwipeRefresh
            // se vea fluido. El SwipeRefresh ya gestiona su estado visual.

            getInvoicesUseCase()
                .debounce(300.milliseconds) // 1. Frena actualizaciones locas de Room
                .catch { error ->
                    Logger.e(TAG, "[ERROR] Flow error: ${error.message}", error)
                    val errorType = ErrorClassifier.classify(error)
                    _uiState.value = InvoiceUIState.Error(
                        message = errorType.toUserMessage(),
                        type = errorType
                    )
                }
                .collect { invoices ->
                    Logger.d("DEBUG_FLOW", "🔥 Recibida lista con ${invoices.size} facturas. Hash: ${invoices.hashCode()}")
                    // 2. Guardamos siempre la lista cruda (Fuente de verdad local)
                    originalInvoices = invoices

                    if (invoices.isEmpty()) {
                        // Si la BD está vacía, mostramos Empty y apagamos el spinner
                        _uiState.value = InvoiceUIState.Empty(isRefreshing = false)
                    } else {
                        // 3. LÓGICA CRÍTICA: Actualizar Stats + Conservar Filtros
                        // Esto evita el "Flashazo" de ver la lista sin filtros
                        actualizarEstadoFiltrosSinPerderSeleccion(invoices)

                        // 4. Aplicar los filtros a la lista y actualizar UI
                        aplicarFiltrosInterno()
                    }
                }
        }
    }

    /**
     * Helper para recalcular estadísticas (nuevos máximos/fechas)
     * PERO respetando lo que el usuario ya tenía seleccionado.
     */
    private fun actualizarEstadoFiltrosSinPerderSeleccion(invoices: List<Invoice>) {
        val currentUI = _filterState.value
        val oldFilters = currentUI.filters
        val oldStats = currentUI.statistics

        // A. Calculamos las nuevas estadísticas de la data fresca
        val newMaxAmount = invoices.maxAmount()
        val newOldest = invoices.oldestDate()
        val newNewest = invoices.newestDate()

        val newStats = InvoiceFilterUIState.FilterStatistics(
            maxAmount = newMaxAmount,
            oldestDateMillis = DateUtils.toEpochMilli(newOldest),
            newestDateMillis = DateUtils.toEpochMilli(newNewest)
        )

        // B. Decidimos qué filtros aplicar
        val filtersToKeep = if (oldFilters == null) {
            // Caso 1: Primera carga (no había filtros) -> Filtros por defecto (Todo abierto)
            InvoiceFilters(
                minAmount = 0f,
                maxAmount = newMaxAmount, // El slider va al tope
                startDate = null,
                endDate = null,
                filteredStates = emptySet()
            )
        } else {
            // Caso 2: Ya había filtros (ej. "Solo Pagadas" o slider a la mitad)

            // Truco de UX: Si el slider estaba al MÁXIMO anterior, lo movemos al NUEVO MÁXIMO.
            // Si el usuario lo había bajado (ej. a 50€), lo respetamos y lo dejamos en 50€.
            val wasSliderAtMax = oldFilters.maxAmount == null ||
                    (oldFilters.maxAmount!! >= oldStats.maxAmount - 0.1f)

            val adjustedMaxAmount = if (wasSliderAtMax) newMaxAmount else oldFilters.maxAmount

            // Copiamos los filtros viejos con el importe ajustado
            oldFilters.copy(
                maxAmount = adjustedMaxAmount
                // Las fechas y los estados (pagadas/pendientes) SE MANTIENEN IGUAL
            )
        }

        // C. Actualizamos el estado de filtros SILENCIOSAMENTE (sin disparar UI todavía)
        _filterState.value = InvoiceFilterUIState(
            filters = filtersToKeep,
            statistics = newStats,
            isApplying = false
        )
    }


    /**
     * Actualiza los filtros temporalmente (UI updates).
     */
    fun updateFilters(filters: InvoiceFilters) {
        val normalized = filters.normalize()
        _filterState.update { it.copy(filters = normalized) }
    }

    /**
     * Aplica los filtros actuales a la lista.
     */
    fun applyFilters() {
        viewModelScope.launch {
            _filterState.update { it.copy(isApplying = true) }

            aplicarFiltrosInterno()
            _filterState.update { it.copy(isApplying = false) }
        }
    }


    // ========== LÓGICA PRIVADA ==========

    private fun aplicarFiltrosInterno() {
        val currentFilters = filterState.value.filters

        val filteredList = if (currentFilters == null) {
            originalInvoices
        } else {
            filterInvoicesUseCase(originalInvoices, currentFilters)
        }

        // ✅ Evita parpadeo al cambiar de Empty a Success
        if (filteredList.isEmpty()) {
            _uiState.value = InvoiceUIState.Empty(isRefreshing = false)
        } else {
            // ✅ Solo actualiza si la lista cambió (reduce re-renderizados)
            val current = uiState.value
            if (current !is InvoiceUIState.Success || current.invoices != filteredList) {
                _uiState.value = InvoiceUIState.Success(
                    invoices = filteredList,
                    isRefreshing = false
                )
            }
        }
    }



    // Helper para compatibilidad con código legacy de errores
    private fun ErrorClassifier.ErrorType.toUserMessage(): String {
        // Implementación simple o importar tu extensión
        return when (this) {
            is ErrorClassifier.ErrorType.Network -> "Error de conexión"
            is ErrorClassifier.ErrorType.Server -> "Error del servidor"
            else -> "Error desconocido"
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // 1. Pones Loading -> Shimmer sale y borra lista
            _uiState.value = InvoiceUIState.Loading

            try {
                // 2. Llamas al UseCase -> Room se actualiza -> Flow de 'observarFacturas' emite
                refreshInvoicesUseCase()
            } catch (e: Exception) {
                // 3. ¡SI FALLA LA RED, TE QUEDAS EN LOADING!
                // Como has puesto Loading arriba, y el Flow no va a emitir nada nuevo si falla el refresh,
                // la pantalla se quedará blanca para siempre.

                // FIX: Si falla, tienes que volver a pintar lo que tenías antes
                // o llamar a observarFacturas() para restaurar el estado.

                Logger.e(TAG, "Refresh failed", e)

                // Lo más seguro si quieres Shimmer es reiniciar la observación,
                // que ya gestiona errores y estados por ti.
                observarFacturas()
            }
        }
    }
    // InvoiceViewModel - The "Clean" Way

    /**
     * SOFT REFRESH: Mantiene la lista visible y actualiza en segundo plano.
     */
    fun onSwipeRefresh() {
        val currentState = _uiState.value

        // Solo permitimos refrescar si ya tenemos datos o está vacío (no en error o loading inicial)
        if (currentState is InvoiceUIState.Success || currentState is InvoiceUIState.Empty) {
            viewModelScope.launch {
                // 1. Solo actualizamos el flag visual de la ruedita, NO tocamos la lista
                // Usamos update para asegurar atomicidad
                _uiState.update {
                    when (it) {
                        is InvoiceUIState.Success -> it.copy(isRefreshing = true)
                        is InvoiceUIState.Empty -> it.copy(isRefreshing = true)
                        else -> it
                    }
                }

                try {
                    // 2. Llamada a red. NO tocamos _uiState aquí manualmente.
                    // Cuando Room se actualice, 'observarFacturas' emitirá el nuevo Success
                    // y ahí pondremos isRefreshing = false automáticamente (si tu UIState por defecto lo tiene false)
                    refreshInvoicesUseCase()
                } catch (e: Exception) {
                    // 3. Solo si falla, apagamos la ruedita manualmente
                    _uiState.update {
                        when (it) {
                            is InvoiceUIState.Success -> it.copy(isRefreshing = false)
                            is InvoiceUIState.Empty -> it.copy(isRefreshing = false)
                            else -> it
                        }
                    }
                    // Opcional: Mostrar error one-shot (Snackbar)
                }
            }
        }
    }

}
