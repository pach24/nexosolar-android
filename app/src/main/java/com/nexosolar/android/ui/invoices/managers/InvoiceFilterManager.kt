package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase

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
        // Como 'newFilters' es inmutable, debemos crear una copia corregida si es necesario.
        val start = newFilters.startDate
        val end = newFilters.endDate

        val filtersToSave = if (start != null && end != null && start.isAfter(end)) {
            // Creamos una COPIA corregida intercambiando fechas
            newFilters.copy(
                startDate = end,
                endDate = start
            )
        } else {
            // Si está bien, usamos el objeto tal cual
            newFilters
        }

        // 2. Guardar el estado limpio
        _currentFilters.postValue(filtersToSave)
    }

    fun resetFilters(invoices: List<Invoice>) {
        // 1. Calculamos el máximo real de las facturas para el slider
        // Usamos Float porque Invoice.invoiceAmount es Float
        val maxReal = invoices.maxOfOrNull { it.invoiceAmount } ?: 0f

        // 2. Creamos los filtros con el rango completo y fechas nulas
        // Usamos el constructor de la data class directamente
        val cleanFilters = InvoiceFilters(
            minAmount = 0f,
            maxAmount = maxReal,      // IMPORTANTE: Para que el slider empiece al final
            startDate = null,         // Para que aparezca "día mes año"
            endDate = null,           // Para que aparezca "día mes año"
            filteredStates = emptySet() // Set vacío inmutable
        )

        _currentFilters.postValue(cleanFilters)
    }

    fun hasActiveFilters(): Boolean {
        val filters = _currentFilters.value ?: return false
        return filters.startDate != null ||
                filters.endDate != null ||
                filters.filteredStates.isNotEmpty()
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
        if (estadosPermitidos.isNotEmpty()) {
            val estadoFactura = invoice.estadoEnum.serverValue // Acceso directo a propiedad val
            if (estadoFactura !in estadosPermitidos) return false
        }

        // 2. Filtro de Importe (Todo en Float ahora)
        val importe = invoice.invoiceAmount
        val min = filters.minAmount ?: 0f
        val max = filters.maxAmount ?: Float.MAX_VALUE

        // Comparación segura de floats
        if (importe !in min..max) return false

        // 3. Filtro de Fechas (Sin cambios, LocalDate es inmutable per se)
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
