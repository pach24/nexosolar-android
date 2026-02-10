package com.nexosolar.android.ui.invoices

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.ui.invoices.managers.InvoiceDataManager
import com.nexosolar.android.ui.invoices.managers.InvoiceFilterManager
import com.nexosolar.android.ui.invoices.managers.InvoiceStateManager
import com.nexosolar.android.ui.invoices.managers.InvoiceStatisticsCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class InvoiceViewModel(
    getInvoicesUseCase: GetInvoicesUseCase,
    filterInvoicesUseCase: FilterInvoicesUseCase
) : ViewModel() {

    // Managers
    private val dataManager = InvoiceDataManager(getInvoicesUseCase)
    private val filterManager = InvoiceFilterManager(filterInvoicesUseCase)
    private val stateManager = InvoiceStateManager()
    private val statisticsCalculator = InvoiceStatisticsCalculator()

    private var isFirstLoad = true

    init {
        cargarFacturas()
    }

    val facturas: LiveData<List<Invoice>> = dataManager.invoices
    val viewState: LiveData<InvoiceStateManager.ViewState> = stateManager.currentState
    val filtrosActuales: LiveData<InvoiceFilters> = filterManager.currentFilters


    val errorValidacion: LiveData<String?> = filterManager.validationError
    val errorMessage: LiveData<String?> = stateManager.errorMessage
    val showEmptyError: LiveData<Boolean> = stateManager.showEmptyError



    fun cargarFacturas() {
        stateManager.showLoading()

        // Lanzamos una corrutina en el scope del ViewModel
        viewModelScope.launch {
            try {
                val result = dataManager.loadInvoices()
                isFirstLoad = false
                if (result.isEmpty()) {
                    stateManager.showEmpty()
                } else {
                    filterManager.resetFilters(result)
                    stateManager.showData()
                }

            } catch (e: Exception) {
                // Cualquier error que caiga en onError del UseCase caerá aquí automáticamente
                isFirstLoad = false
                handleLoadError(e)
            }
        }
    }

    fun actualizarFiltros(filters: InvoiceFilters) {
        filterManager.updateFilters(filters)
        stateManager.showLoading()

        viewModelScope.launch {
            delay(300)
            val filtered = withContext(Dispatchers.Default) {
                // Procesamiento pesado en hilo de cómputo
                filterManager.applyCurrentFilters(dataManager.originalInvoices)
            }

            dataManager.setInvoices(filtered)

            if (filtered.isEmpty()) {
                stateManager.showEmpty()
            } else {
                stateManager.showData()
            }
        }
    }

    fun resetearFiltros() {
        val original = dataManager.originalInvoices
        filterManager.resetFilters(original)
        dataManager.setInvoices(original)
        stateManager.showData()
    }

    fun actualizarEstadoFiltros(filters: InvoiceFilters) {
        filterManager.updateFilters(filters)
    }

    // Delegación a StatisticsCalculator
    fun getMaxImporte(): Float = statisticsCalculator.calculateMaxAmount(dataManager.originalInvoices)
    fun getOldestDate(): LocalDate? = statisticsCalculator.calculateOldestDate(dataManager.originalInvoices)
    fun getNewestDate(): LocalDate? = statisticsCalculator.calculateNewestDate(dataManager.originalInvoices)

    fun hayDatosCargados(): Boolean = dataManager.originalInvoices.isNotEmpty()
    fun hayFiltrosActivos(): Boolean = filterManager.hasActiveFilters()

    private fun handleLoadError(error: Throwable) {
        val errorType = ErrorClassifier.classify(error)

        if (dataManager.hasCachedData()) {
            stateManager.showData()
            return
        }

        val message = ErrorClassifier.getErrorMessage(errorType, error)
        when (errorType) {
            ErrorClassifier.ErrorType.SERVER -> stateManager.showServerError(message)
            ErrorClassifier.ErrorType.NETWORK -> {

                viewModelScope.launch {
                    delay(3000)
                    stateManager.showNetworkError(message)
                }
            }
            else -> stateManager.showServerError("Error inesperado: $message")
        }
    }



    fun aplicarFiltrosSeleccionados(estados: List<String>, min: Double, max: Double) {

        // Obtenemos el filtro actual o creamos uno nuevo
        val filtroActual = filtrosActuales.value ?: InvoiceFilters()

        // CAMBIO 3: Usamos copy() para crear una nueva instancia inmutable
        // en lugar de reasignar propiedades con setters (nuevosFiltros.minAmount = ...)
        val nuevosFiltros = filtroActual.copy(
            filteredStates = estados.toSet(), // Convertimos List -> Set
            minAmount = min.toFloat(),        // Convertimos Double -> Float
            maxAmount = max.toFloat()         // Convertimos Double -> Float
        )

        // Delegamos al manager
        filterManager.updateFilters(nuevosFiltros)

        // Ejecutamos el filtrado
        actualizarFiltros(nuevosFiltros)
    }

    fun getOldestDateMillis(): Long {
        val date = this.getOldestDate()
        return DateUtils.toEpochMilli(date)
    }

    fun getNewestDateMillis(): Long {
        val date = this.getNewestDate()
        return DateUtils.toEpochMilli(date)
    }

    fun esEstadoError() = stateManager.isError()
}