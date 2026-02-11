package com.nexosolar.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.maxAmount
import com.nexosolar.android.domain.models.minAmount
import com.nexosolar.android.domain.models.newestDate
import com.nexosolar.android.domain.models.oldestDate
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
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

/**
 * ViewModel reactivo para la gestión de facturas usando StateFlow.
 *
 * MIGRACIÓN A STATEFLOW:
 * - Reemplaza LiveData con StateFlow (estándar moderno).
 * - Consume el Flow del UseCase de forma reactiva.
 * - Centraliza el estado en InvoiceUiState (sealed interface).
 */
class InvoiceViewModel(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "InvoiceVM"
    }

    // ========== ESTADO DE LA UI (Sealed Interface) ==========

    sealed interface InvoiceUiState {
        object Loading : InvoiceUiState
        object Empty : InvoiceUiState
        data class Success(val invoices: List<Invoice>) : InvoiceUiState
        data class Error(val message: String, val type: ErrorClassifier.ErrorType) : InvoiceUiState
    }

    // ========== STATEFLOWS (Fuente de verdad) ==========

    // Estado principal de la UI (Carga, Éxito, Error)
    private val _uiState = MutableStateFlow<InvoiceUiState>(InvoiceUiState.Loading)
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

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
            _uiState.value = InvoiceUiState.Loading

            getInvoicesUseCase()
                .catch { error ->
                    Logger.e(TAG, "[ERROR] Flow error: ${error.message}", error)
                    val errorType = ErrorClassifier.classify(error)
                    _uiState.value = InvoiceUiState.Error(
                        message = errorType.toUserMessage(), // Asegúrate de tener esta extension o usa string directo
                        type = errorType
                    )
                }
                .collect { invoices ->
                    Logger.d(TAG, "[FLOW] Received ${invoices.size} invoices")
                    originalInvoices = invoices

                    if (invoices.isEmpty()) {
                        _uiState.value = InvoiceUiState.Empty
                    } else {
                        // Inicializamos filtros solo si es la primera carga (opcional)
                        if (_filterState.value.statistics.maxAmount == 0f) {
                            initializeFilterState(invoices)
                        }
                        // Aplicamos filtros actuales si los hay, o mostramos todo
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
        val filtered = if (currentFilters == null) {
            originalInvoices
        } else {
            // Nota: filterInvoicesUseCase es síncrono o suspend? Asumimos síncrono o rápido
            // Si es pesado, usar withContext(Dispatchers.Default)
            filterInvoicesUseCase(originalInvoices, currentFilters)
        }

        if (filtered.isEmpty() && originalInvoices.isNotEmpty()) {
            // Caso especial: Hay datos pero el filtro los ocultó todos
            // Podrías tener un estado EmptyFiltered o simplemente Empty
            _uiState.value = InvoiceUiState.Empty
        } else {
            _uiState.value = InvoiceUiState.Success(filtered)
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

    // Añadir en InvoiceViewModel.kt
    fun refresh() {
        observarFacturas() // Reinicia la observación y carga
    }
}
