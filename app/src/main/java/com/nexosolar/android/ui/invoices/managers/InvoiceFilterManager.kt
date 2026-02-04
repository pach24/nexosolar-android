package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.core.DateValidator
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import java.time.LocalDate

/**
 * Gestor especializado en el ESTADO de los filtros.
 *
 * - Solo gestiona el estado y orquesta el filtrado.
 * - Delegación: Usa DateValidator para validación y InvoiceStatisticsCalculator para valores por defecto.
 */
class InvoiceFilterManager(
    private val filterUseCase: FilterInvoicesUseCase
) {
    private val calculator = InvoiceStatisticsCalculator()

    private val _currentFilters = MutableLiveData<InvoiceFilters>()
    private val _validationError = MutableLiveData<String?>()

    init {
        initializeDefaultFilters()
    }

    // ===== Getters =====
    val currentFilters: LiveData<InvoiceFilters> get() = _currentFilters
    val validationError: LiveData<String?> get() = _validationError

    // ===== Gestión de Estado =====
    fun updateFilters(filters: InvoiceFilters?) {
        // 1. Si es nulo, error y salir.
        if (filters == null) {
            _validationError.value = "Los filtros no pueden ser nulos"
            return
        }

        // 2. A partir de aquí 'filters' es seguro, pero para evitar problemas con LiveData
        // creamos una referencia local no nula explícita.
        val safeFilters: InvoiceFilters = filters

        // Auto-corrección defensiva
        val start = safeFilters.startDate
        val end = safeFilters.endDate

        if (start != null && end != null && start.isAfter(end)) {
            safeFilters.startDate = end
            safeFilters.endDate = start
        }

        // USO DE DATE VALIDATOR
        // Usamos safeFilters en todo el bloque
        if (DateValidator.isValidRange(safeFilters.startDate, safeFilters.endDate)) {
            _currentFilters.value = safeFilters // ¡Ahora sí! Coinciden los tipos
            _validationError.value = null
        } else {
            _validationError.value = "La fecha de inicio no puede ser posterior a la fecha final"
        }
    }


    fun resetFilters(invoices: List<Invoice>?) {
        val defaultFilters = InvoiceFilters()
        defaultFilters.startDate = null
        defaultFilters.endDate = null
        defaultFilters.minAmount = 0.0

        // USO DE CALCULATOR: Delegamos el cálculo del máximo
        val maxAmount = calculator.calculateMaxAmount(invoices)
        defaultFilters.maxAmount = maxAmount.toDouble()

        // Lista vacía = “sin filtrar por estado” (y en applyCurrentFilters se interpreta como TODOS)
        defaultFilters.filteredStates = ArrayList()

        _currentFilters.value = defaultFilters
        _validationError.value = null
    }

    // ===== Ejecución de Filtros =====
    fun applyCurrentFilters(invoices: List<Invoice>?): List<Invoice> {
        val filters = _currentFilters.value
        if (filters == null || invoices.isNullOrEmpty()) {
            return emptyList()
        }

        var statesToFilter = filters.filteredStates

        // REGLA: Lista vacía = Todos los estados
        if (statesToFilter.isNullOrEmpty()) {
            statesToFilter = ArrayList()
            for (state in InvoiceState.values()) {
                statesToFilter.add(state.serverValue)
            }
        }

        // REGLA: Fechas nulas = Extremos del dataset
        // Calculamos fechas efectivas TEMPORALES solo para esta ejecución
        var effectiveStart = filters.startDate
        var effectiveEnd = filters.endDate

        if (effectiveStart == null) {
            effectiveStart = calculator.calculateOldestDate(invoices)
        }

        if (effectiveEnd == null) {
            effectiveEnd = calculator.calculateNewestDate(invoices)
            if (effectiveEnd == null) effectiveEnd = LocalDate.now()
        }

        return filterUseCase.execute(
            invoices,
            statesToFilter,
            effectiveStart, // Usamos las fechas efectivas
            effectiveEnd,   // Usamos las fechas efectivas
            filters.minAmount,
            filters.maxAmount
        )
    }

    // ===== Métodos de Consulta de Estado =====
    fun hasActiveFilters(): Boolean {
        val filters = _currentFilters.value ?: return false

        if (!filters.filteredStates.isNullOrEmpty()) return true
        if (filters.startDate != null || filters.endDate != null) return true

        // Comprobamos si el rango es distinto al por defecto (0 - MAX)
        val maxAmount = filters.maxAmount
        return filters.minAmount > 0 || (maxAmount != null && maxAmount < Double.MAX_VALUE)
    }

    private fun initializeDefaultFilters() {
        val defaultFilters = InvoiceFilters()
        defaultFilters.minAmount = 0.0
        defaultFilters.maxAmount = Double.MAX_VALUE
        defaultFilters.filteredStates = ArrayList()
        _currentFilters.value = defaultFilters
    }
}
