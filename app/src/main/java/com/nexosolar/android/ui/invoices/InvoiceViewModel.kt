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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            _uiState.value = InvoiceUIState.Loading

            getInvoicesUseCase()
                .catch { error ->
                    Logger.e(TAG, "[ERROR] Flow error: ${error.message}", error)
                    val errorType = ErrorClassifier.classify(error)
                    _uiState.value = InvoiceUIState.Error(
                        message = errorType.toUserMessage(), // Asegúrate de tener esta extension o usa string directo
                        type = errorType
                    )
                }
                .collect { invoices ->
                    // 1. Guardamos la nueva lista cruda
                    originalInvoices = invoices

                    if (invoices.isEmpty()) {
                        _uiState.value = InvoiceUIState.Empty()
                    } else {
                        // 2. LÓGICA DE PERSISTENCIA CORREGIDA
                        val currentStats = _filterState.value.statistics
                        val currentFilters = _filterState.value.filters

                        // Solo inicializamos si NO hay estadísticas previas (primera carga)
                        // O si queremos recalcular máximos pero MANTENIENDO la selección del usuario
                        if (currentStats.maxAmount == 0f) {
                            // Caso 1: Primera vez absoluta -> Creamos todo de cero
                            initializeFilterState(invoices)
                        } else {
                            // Caso 2: Ya existían filtros -> Actualizamos solo las estadísticas (nuevos rangos de fecha/importe)
                            // pero NO tocamos 'currentFilters' (lo que el usuario seleccionó)
                            updateStatisticsOnly(invoices)
                        }

                        // 3. Aplicamos los filtros que tenga guardados (o los nuevos por defecto)
                        aplicarFiltrosInterno()
                    }
                }
        }
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
            delay(200) // UX delay
            aplicarFiltrosInterno()
            _filterState.update { it.copy(isApplying = false) }
        }
    }

    /**
     * Resetea filtros y muestra lista original.
     */
    fun resetearFiltros() {
        if (originalInvoices.isNotEmpty()) {
            initializeFilterState(originalInvoices)
            aplicarFiltrosInterno()
        }
    }

    // ========== LÓGICA PRIVADA ==========

    private fun aplicarFiltrosInterno() {
        val currentFilters = _filterState.value.filters

        // Si no hay filtros, mostramos la lista completa (originalInvoices)
        // Si HAY filtros, usamos el UseCase sobre la lista NUEVA (originalInvoices)
        val filteredList = if (currentFilters == null) {
            originalInvoices
        } else {
            filterInvoicesUseCase(originalInvoices, currentFilters)
        }

        _uiState.value = if (filteredList.isEmpty()) {
            InvoiceUIState.Empty() // O un estado EmptyFiltered si quieres diferenciar
        } else {
            InvoiceUIState.Success(filteredList)
        }
    }

    private fun initializeFilterState(invoices: List<Invoice>) {
        val maxAmount = invoices.maxAmount()
        val oldest = invoices.oldestDate()
        val newest = invoices.newestDate()

        _filterState.value = InvoiceFilterUIState(
            filters = InvoiceFilters(
                minAmount = 0f,
                maxAmount = maxAmount,
                startDate = null,
                endDate = null,
                filteredStates = emptySet()
            ),
            statistics = InvoiceFilterUIState.FilterStatistics(
                maxAmount = maxAmount,
                oldestDateMillis = DateUtils.toEpochMilli(oldest),
                newestDateMillis = DateUtils.toEpochMilli(newest)
            ),
            isApplying = false
        )
    }

    // Helper para compatibilidad con código legacy de errores
    private fun ErrorClassifier.ErrorType.toUserMessage(): String {
        // Implementación simple o importar tu extensión
        return when(this) {
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

        // AHORA: Si es Success O Empty, hacemos Soft Refresh (solo ruedita)
        if (currentState is InvoiceUIState.Success || currentState is InvoiceUIState.Empty) {
            viewModelScope.launch {
                // 1. Encender ruedita sin cambiar de pantalla
                if (currentState is InvoiceUIState.Success) {
                    _uiState.value = currentState.copy(isRefreshing = true)
                } else if (currentState is InvoiceUIState.Empty) {
                    _uiState.value = currentState.copy(isRefreshing = true)
                }

                // 2. Llamar a la API
                try {
                    refreshInvoicesUseCase()
                    // Al volver, el Flow 'observarFacturas' emitirá los nuevos datos
                    // y la ruedita se apagará sola porque el nuevo estado vendrá con isRefreshing=false por defecto
                } catch (e: Exception) {
                    // Si falla, apagamos la ruedita manualmente
                    if (currentState is InvoiceUIState.Success) {
                        _uiState.value = currentState.copy(isRefreshing = false)
                    } else if (currentState is InvoiceUIState.Empty) {
                        _uiState.value = currentState.copy(isRefreshing = false)
                    }
                }
            }
        } else {
            // Solo en caso de ERROR hacemos Hard Refresh (Shimmer)
            observarFacturas()
        }
    }
    private fun updateStatisticsOnly(invoices: List<Invoice>) {
        val newMaxAmount = invoices.maxAmount()
        val newOldest = invoices.oldestDate()
        val newNewest = invoices.newestDate()

        _filterState.update { currentState ->
            currentState.copy(
                statistics = InvoiceFilterUIState.FilterStatistics(
                    maxAmount = newMaxAmount,
                    oldestDateMillis = DateUtils.toEpochMilli(newOldest),
                    newestDateMillis = DateUtils.toEpochMilli(newNewest)
                )
                // IMPORTANTE: No tocamos 'filters' aquí, así que se mantienen.
            )
        }
    }







}
