package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import java.time.LocalDate

/**
 * Gestor especializado en la lógica de filtrado de facturas.
 */
class InvoiceFilterManager(private val filterInvoicesUseCase: FilterInvoicesUseCase) {

    private val _currentFilters = MutableLiveData<InvoiceFilters>()
    val currentFilters: LiveData<InvoiceFilters> get() = _currentFilters

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> get() = _validationError



    fun updateFilters(newFilters: InvoiceFilters) {
        // 1. Lógica de Negocio: Corregir el orden de las fechas si el usuario se equivocó
        val start = newFilters.startDate
        val end = newFilters.endDate

        if (start != null && end != null && start.isAfter(end)) {
            newFilters.startDate = end
            newFilters.endDate = start
        }

        // 2. Guardar el estado limpio
        _currentFilters.postValue(newFilters)
    }

    fun resetFilters(invoices: List<Invoice>) {
        // 1. Calculamos el máximo real de las facturas para el slider
        val maxReal = invoices.maxOfOrNull { it.invoiceAmount }?.toDouble() ?: 0.0

        // 2. Creamos los filtros con el rango completo y fechas nulas
        val cleanFilters = InvoiceFilters().apply {
            minAmount = 0.0
            maxAmount = maxReal  // IMPORTANTE: Para que el slider empiece al final
            startDate = null     // Para que aparezca "día mes año"
            endDate = null       // Para que aparezca "día mes año"
            filteredStates = mutableListOf()
        }

        _currentFilters.postValue(cleanFilters)
    }

    fun hasActiveFilters(): Boolean {
        val filters = _currentFilters.value ?: return false
        return filters.startDate != null ||
                filters.endDate != null ||
                !filters.filteredStates.isNullOrEmpty()
    }

    /**
     * Aplica los filtros actuales a una lista de entrada.
     */
    fun applyCurrentFilters(allInvoices: List<Invoice>): List<Invoice> {
        val filters = _currentFilters.value ?: return allInvoices

        return allInvoices.filter { invoice ->
            cumpleFiltro(invoice, filters)
        }
    }

    private fun cumpleFiltro(invoice: Invoice, filters: InvoiceFilters): Boolean {
        // 1. Filtro de Estado
        val estadosPermitidos = filters.filteredStates
        if (!estadosPermitidos.isNullOrEmpty()) {
            val estadoFactura = invoice.estadoEnum?.serverValue
            if (estadoFactura !in estadosPermitidos) return false
        }

        // 2. Filtro de Importe
        val importe = invoice.invoiceAmount
        val min = filters.minAmount ?: 0.0
        val max = filters.maxAmount ?: Double.MAX_VALUE
        if (importe < min || importe > max) return false

        // 3. Filtro de Fechas
        val fechaFactura = invoice.invoiceDate
        if (fechaFactura != null) {
            val start = filters.startDate
            val end = filters.endDate
            if (start != null && fechaFactura.isBefore(start)) return false
            if (end != null && fechaFactura.isAfter(end)) return false
        }

        return true
    }



}