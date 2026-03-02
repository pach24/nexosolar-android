package com.nexosolar.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.Logger
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.maxAmount
import com.nexosolar.android.domain.models.newestDate
import com.nexosolar.android.domain.models.oldestDate
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.RefreshInvoicesUseCase
import com.nexosolar.android.ui.common.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase,
    private val refreshInvoicesUseCase: RefreshInvoicesUseCase
) : ViewModel() {

    private val tag = "InvoiceVM"

    // 1. ESTADOS INDEPENDIENTES (Fuentes de verdad)

    // Filtros que el usuario está modificando en la UI (Draft)
    private val _filterState = MutableStateFlow(InvoiceFilterUIState())
    val filterState: StateFlow<InvoiceFilterUIState> = _filterState.asStateFlow()

    // Filtros confirmados que realmente se aplican a la lista
    private val _appliedFilters = MutableStateFlow(InvoiceFilters())

    // Estado del Pull-to-Refresh
    private val _isRefreshing = MutableStateFlow(false)

    // 2. OPTIMIZACIÓN THREAD-SAFE (Sin bloquear hilos)
    private val amountFormatter = object : ThreadLocal<NumberFormat>() {
        override fun initialValue(): NumberFormat {
            return NumberFormat.getCurrencyInstance(Locale("es", "ES")).apply {
                currency = Currency.getInstance("EUR")
                maximumFractionDigits = 2
                minimumFractionDigits = 2
            }
        }
    }

    // 3. ESTADO REACTIVO COMBINADO (El corazón del ViewModel)
    val uiState: StateFlow<InvoiceUIState> = combine(
        // Envolvemos el caso de uso suspendido en un flow builder
        flow { emitAll(getInvoicesUseCase()) }.debounce(300.milliseconds),
        _appliedFilters,
        _isRefreshing
    ) { invoices, filters, isRefreshing ->

        if (invoices.isEmpty()) {
            return@combine InvoiceUIState.Empty(isRefreshing = isRefreshing)
        }

        // Actualizamos las estadísticas de los filtros (side-effect seguro)
        updateFilterBounds(invoices)

        // Aplicamos los filtros confirmados
        val filteredList = filterInvoicesUseCase(invoices, filters)

        if (filteredList.isEmpty()) {
            InvoiceUIState.Empty(isRefreshing = isRefreshing)
        } else {
            val uiItems = filteredList.map { it.toUiItem() }
            InvoiceUIState.Success(invoices = uiItems, isRefreshing = isRefreshing)
        }
    }
        .catch { error ->
            Logger.e(tag, "[ERROR] Flow error: ${error.message}", error)
            val errorType = ErrorClassifier.classify(error)
            emit(InvoiceUIState.Error(errorType.toUserMessageRes(), errorType))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Optimización crítica para Compose
            initialValue = InvoiceUIState.Loading
        )

    private fun updateFilterBounds(invoices: List<Invoice>) {
        val currentUI = _filterState.value
        val oldFilters = currentUI.filters
        val oldStats = currentUI.statistics

        val newMaxAmount = invoices.maxAmount()
        val newStats = InvoiceFilterUIState.FilterStatistics(
            maxAmount = newMaxAmount,
            oldestDateMillis = DateUtils.toEpochMilli(invoices.oldestDate()),
            newestDateMillis = DateUtils.toEpochMilli(invoices.newestDate())
        )

        val finalMin = oldFilters.minAmount?.coerceIn(0f, newMaxAmount) ?: 0f
        val finalMax = if (oldFilters.maxAmount != null && oldFilters.maxAmount!! >= oldStats.maxAmount * 0.99f) {
            newMaxAmount
        } else {
            oldFilters.maxAmount?.coerceIn(finalMin, newMaxAmount) ?: newMaxAmount
        }

        _filterState.update {
            it.copy(
                filters = oldFilters.copy(minAmount = finalMin, maxAmount = finalMax),
                statistics = newStats
            )
        }
    }

    private fun Invoice.toUiItem(): InvoiceListItemUi {
        val formattedDate = DateUtils.formatDate(this.invoiceDate)
        // Usamos ThreadLocal para evitar synchronized y bloqueos
        val formattedAmount = amountFormatter.get()?.format(this.invoiceAmount.toDouble()) ?: ""

        return InvoiceListItemUi(
            id = this.invoiceID,
            dateText = formattedDate,
            amountText = formattedAmount,
            state = this.estadoEnum
        )
    }

    // --- INTENCIONES DEL USUARIO (Eventos de la UI) ---

    fun updateFilters(filters: InvoiceFilters) {
        _filterState.update { it.copy(filters = filters.normalize()) }
    }

    fun applyFilters() {
        // Al actualizar esta variable, el 'combine' reacciona automáticamente
        _appliedFilters.value = _filterState.value.filters
    }

    fun onSwipeRefresh() {
        viewModelScope.launch { // Ya no forzamos Dispatchers.Default
            _isRefreshing.value = true
            try {
                refreshInvoicesUseCase()
            } catch (e: Exception) {
                Logger.e(tag, "Refresh failed", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                refreshInvoicesUseCase()
            } catch (e: Exception) {
                Logger.e(tag, "Full refresh failed", e)
            }
        }
    }
}
