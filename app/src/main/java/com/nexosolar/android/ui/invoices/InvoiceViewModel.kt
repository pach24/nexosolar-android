package com.nexosolar.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.Logger
import com.nexosolar.android.core.toTechnicalMessage

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.maxAmount
import com.nexosolar.android.domain.models.minAmount
import com.nexosolar.android.domain.models.newestDate
import com.nexosolar.android.domain.models.oldestDate
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.RefreshInvoicesUseCase
import com.nexosolar.android.ui.common.toUserMessageRes
import com.nexosolar.android.ui.invoices.InvoiceFilterUIState
import com.nexosolar.android.ui.invoices.InvoiceUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase,
    private val refreshInvoicesUseCase: RefreshInvoicesUseCase
) : ViewModel() {

    private val TAG = "InvoiceVM"

    // --- ESTADO DE LA UI (Lista de facturas) ---
    private val _uiState = MutableStateFlow<InvoiceUIState>(InvoiceUIState.Loading)
    val uiState: StateFlow<InvoiceUIState> = _uiState.asStateFlow()

    // --- ESTADO DE LOS FILTROS (Independiente) ---
    private val _filterState = MutableStateFlow(InvoiceFilterUIState())
    val filterState: StateFlow<InvoiceFilterUIState> = _filterState.asStateFlow()

    // Copia local para filtrado rápido (exactamente como en el antiguo)
    private var originalInvoices: List<Invoice> = emptyList()

    private var collectionJob: Job? = null

    init {
        observarFacturas()
    }

    /**
     * Lógica idéntica al antiguo:
     * 1. Observa Room
     * 2. Guarda originalInvoices
     * 3. Llama a actualizarEstadoFiltrosSinPerderSeleccion (tu lógica clave)
     * 4. Aplica filtros
     */
    private fun observarFacturas() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            getInvoicesUseCase()
                .debounce(300.milliseconds)
                .catch { error ->
                    Logger.e(TAG, "[ERROR] Flow error: ${error.message}", error)
                    val errorType = ErrorClassifier.classify(error)
                    _uiState.value = InvoiceUIState.Error(
                        messageRes = errorType.toUserMessageRes(),
                        type = errorType
                    )
                }
                .collect { invoices ->
                    originalInvoices = invoices

                    if (invoices.isEmpty()) {
                        _uiState.value = InvoiceUIState.Empty(isRefreshing = false)
                    } else {
                        // ✅ ESTA ES LA FUNCIÓN QUE QUERÍAS PRESERVAR
                        actualizarEstadoFiltrosSinPerderSeleccion(invoices)
                        aplicarFiltrosInterno()
                    }
                }
        }
    }

    private fun actualizarEstadoFiltrosSinPerderSeleccion(invoices: List<Invoice>) {
        if (invoices.isEmpty()) return

        val currentUI = _filterState.value
        val oldFilters = currentUI.filters
        val oldStats = currentUI.statistics

        // 1. Nuevas estadísticas
        val newMaxAmount = invoices.maxAmount()
        val newStats = InvoiceFilterUIState.FilterStatistics(
            maxAmount = newMaxAmount,
            oldestDateMillis = DateUtils.toEpochMilli(invoices.oldestDate()),
            newestDateMillis = DateUtils.toEpochMilli(invoices.newestDate())
        )

        // 2. Lógica de conservación/reseteo de filtros
        val finalMin: Float
        val finalMax: Float

        if (oldFilters.minAmount == null || oldStats.maxAmount <= 0f) {
            // ✅ Primera carga: rango completo
            finalMin = 0f
            finalMax = newMaxAmount
        } else {
            val oldMin = oldFilters.minAmount!!
            val oldMax = oldFilters.maxAmount!!
            val wasAtMax = oldMax >= oldStats.maxAmount * 0.99f  // Tolerancia 1%

            // 🔥 CASO CRÍTICO: ¿El filtro anterior ya no es válido?
            if (oldMin >= newMaxAmount) {
                // ✅ Reseteo completo: el usuario había filtrado por encima del nuevo tope
                finalMin = 0f
                finalMax = newMaxAmount
            } else {
                // ✅ Conservar filtros inteligentemente
                finalMin = oldMin.coerceIn(0f, newMaxAmount)
                finalMax = if (wasAtMax) {
                    newMaxAmount  // Estirar al nuevo máximo
                } else {
                    oldMax.coerceIn(finalMin, newMaxAmount)  // Mantener valor exacto
                }
            }
        }

        // 3. Actualizar estado
        val filtersToKeep = oldFilters.copy(minAmount = finalMin, maxAmount = finalMax)
        _filterState.value = currentUI.copy(filters = filtersToKeep, statistics = newStats)
    }









    /**
     * Actualiza los filtros temporalmente desde la UI (Compose)
     */
    fun updateFilters(filters: InvoiceFilters) {
        // Normalizamos para evitar errores de rango
        val normalized = filters.normalize()
        _filterState.update { it.copy(filters = normalized) }
    }

    /**
     * Aplica los filtros actuales a la lista y actualiza la UI
     */
    fun applyFilters() {
        viewModelScope.launch {
            _filterState.update { it.copy(isApplying = true) }
            aplicarFiltrosInterno()
            _filterState.update { it.copy(isApplying = false) }
        }
    }

    private fun aplicarFiltrosInterno() {
        val currentFilters = _filterState.value.filters ?: return
        val filteredList = filterInvoicesUseCase(originalInvoices, currentFilters)

        if (filteredList.isEmpty()) {
            _uiState.value = InvoiceUIState.Empty(isRefreshing = false)
        } else {
            // Solo actualizamos si la lista cambió para evitar parpadeos
            val current = _uiState.value
            if (current !is InvoiceUIState.Success || current.invoices != filteredList) {
                _uiState.value = InvoiceUIState.Success(
                    invoices = filteredList,
                    isRefreshing = false
                )
            }
        }
    }

    // --- REFRESH / SWIPE TO REFRESH ---

    fun onSwipeRefresh() {
        val currentState = _uiState.value
        if (currentState is InvoiceUIState.Success || currentState is InvoiceUIState.Empty) {
            viewModelScope.launch {
                setRefreshing(true)
                try {
                    refreshInvoicesUseCase()
                } catch (e: Exception) {
                    Logger.e(TAG, "Refresh failed", e)
                    setRefreshing(false)
                }
            }
        }
    }

    fun refresh() {
        _uiState.value = InvoiceUIState.Loading
        viewModelScope.launch {
            try {
                refreshInvoicesUseCase()
            } catch (e: Exception) {
                Logger.e(TAG, "Refresh failed", e)
                observarFacturas() // Reiniciamos observación si falla
            }
        }
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        _uiState.update { state ->
            when (state) {
                is InvoiceUIState.Success -> state.copy(isRefreshing = isRefreshing)
                is InvoiceUIState.Empty -> state.copy(isRefreshing = isRefreshing)
                else -> state
            }
        }
    }
}
