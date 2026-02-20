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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
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

    private val _uiState = MutableStateFlow<InvoiceUIState>(InvoiceUIState.Loading)
    val uiState: StateFlow<InvoiceUIState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(InvoiceFilterUIState())
    val filterState: StateFlow<InvoiceFilterUIState> = _filterState.asStateFlow()

    private var originalInvoices: List<Invoice> = emptyList()
    private var collectionJob: Job? = null

    private val amountFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES")).apply {
        currency = Currency.getInstance("EUR")
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    init {
        observarFacturas()
    }

    private fun observarFacturas() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch(Dispatchers.Default) {
            getInvoicesUseCase()
                .debounce(300.milliseconds)
                .catch { error ->
                    Logger.e(tag, "[ERROR] Flow error: ${error.message}", error)
                    val errorType = ErrorClassifier.classify(error)
                    _uiState.value = InvoiceUIState.Error(
                        messageRes = errorType.toUserMessageRes(),
                        type = errorType
                    )
                }
                .collect { invoices ->
                    originalInvoices = invoices
                    processInvoices(invoices)
                }
        }
    }

    private fun processInvoices(invoices: List<Invoice>) {
        if (invoices.isEmpty()) {
            _uiState.value = InvoiceUIState.Empty(isRefreshing = false)
            return
        }

        val currentFilterUi = _filterState.value
        val updatedFilterState = calculateFilterState(currentFilterUi, invoices)
        val currentFilters = updatedFilterState.filters
        val filteredList = filterInvoicesUseCase(originalInvoices, currentFilters)

        _filterState.value = updatedFilterState

        if (filteredList.isEmpty()) {
            _uiState.value = InvoiceUIState.Empty(isRefreshing = false)
            return
        }

        val uiItems = filteredList.map(::toUiItem)
        val current = _uiState.value
        if (current !is InvoiceUIState.Success || current.invoices != uiItems) {
            _uiState.value = InvoiceUIState.Success(
                invoices = uiItems,
                isRefreshing = false
            )
        }
    }

    private fun calculateFilterState(
        currentUI: InvoiceFilterUIState,
        invoices: List<Invoice>
    ): InvoiceFilterUIState {
        val oldFilters = currentUI.filters
        val oldStats = currentUI.statistics

        val newMaxAmount = invoices.maxAmount()
        val newStats = InvoiceFilterUIState.FilterStatistics(
            maxAmount = newMaxAmount,
            oldestDateMillis = DateUtils.toEpochMilli(invoices.oldestDate()),
            newestDateMillis = DateUtils.toEpochMilli(invoices.newestDate())
        )

        val finalMin: Float
        val finalMax: Float

        if (oldFilters.minAmount == null || oldStats.maxAmount <= 0f) {
            finalMin = 0f
            finalMax = newMaxAmount
        } else {
            val oldMin = oldFilters.minAmount!!
            val oldMax = oldFilters.maxAmount!!
            val wasAtMax = oldMax >= oldStats.maxAmount * 0.99f

            if (oldMin >= newMaxAmount) {
                finalMin = 0f
                finalMax = newMaxAmount
            } else {
                finalMin = oldMin.coerceIn(0f, newMaxAmount)
                finalMax = if (wasAtMax) {
                    newMaxAmount
                } else {
                    oldMax.coerceIn(finalMin, newMaxAmount)
                }
            }
        }

        val filtersToKeep = oldFilters.copy(minAmount = finalMin, maxAmount = finalMax)
        return currentUI.copy(filters = filtersToKeep, statistics = newStats)
    }

    private fun toUiItem(invoice: Invoice): InvoiceListItemUi {
        val formattedDate = DateUtils.formatDate(invoice.invoiceDate)
        val formattedAmount = synchronized(amountFormatter) {
            amountFormatter.format(invoice.invoiceAmount.toDouble())
        }
        return InvoiceListItemUi(
            id = invoice.invoiceID,
            dateText = formattedDate,
            amountText = formattedAmount,
            state = invoice.estadoEnum
        )
    }

    fun updateFilters(filters: InvoiceFilters) {
        val normalized = filters.normalize()
        _filterState.update { it.copy(filters = normalized) }
    }

    fun applyFilters() {
        viewModelScope.launch(Dispatchers.Default) {
            _filterState.update { it.copy(isApplying = true) }
            processInvoices(originalInvoices)
            _filterState.update { it.copy(isApplying = false) }
        }
    }

    fun onSwipeRefresh() {
        val currentState = _uiState.value
        if (currentState is InvoiceUIState.Success || currentState is InvoiceUIState.Empty) {
            viewModelScope.launch {
                setRefreshing(true)
                try {
                    refreshInvoicesUseCase()
                } catch (e: Exception) {
                    Logger.e(tag, "Refresh failed", e)
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
                Logger.e(tag, "Refresh failed", e)
                observarFacturas()
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
