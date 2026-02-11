package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters

/**
 * Gestor especializado en la gestión del estado de filtros de UI.
 *
 * Responsabilidades:
 * - Mantener el estado actual de los filtros (LiveData)
 * - Corregir inconsistencias de UI (fechas invertidas)
 * - Validar entrada del usuario
 * - NO contiene lógica de negocio de filtrado (eso es del UseCase)
 */
class InvoiceFilterManager {

    // Estado observable de los filtros actuales
    private val _currentFilters = MutableLiveData<InvoiceFilters>()
    val currentFilters: LiveData<InvoiceFilters> get() = _currentFilters

    // Estado observable para errores de validación de UI
    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> get() = _validationError

    /**
     * Actualiza los filtros aplicando correcciones de UI si es necesario.
     * Ejemplo: intercambia fechas si el usuario seleccionó fin antes de inicio.
     */
    fun updateFilters(newFilters: InvoiceFilters) {
        // Lógica de UI: Corregir el orden de las fechas si están invertidas
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

        // Guardar el estado limpio
        _currentFilters.postValue(filtersToSave)
    }

    /**
     * Resetea los filtros al estado inicial basándose en la lista completa.
     * Calcula el rango máximo de importe para el slider.
     */
    fun resetFilters(invoices: List<Invoice>) {
        // Calculamos el máximo real de las facturas para el slider
        val maxReal = invoices.maxOfOrNull { it.invoiceAmount } ?: 0f

        // Creamos los filtros con el rango completo y fechas nulas
        val cleanFilters = InvoiceFilters(
            minAmount = 0f,
            maxAmount = maxReal,
            startDate = null,
            endDate = null,
            filteredStates = emptySet() // Set vacío inmutable
        )

        _currentFilters.postValue(cleanFilters)
    }

    /**
     * Indica si hay filtros activos (diferentes al estado inicial).
     */
    fun hasActiveFilters(): Boolean {
        val filters = _currentFilters.value ?: return false
        return filters.startDate != null ||
                filters.endDate != null ||
                filters.filteredStates.isNotEmpty()
    }

    /**
     * Obtiene los filtros actuales de forma síncrona.
     * Útil para el ViewModel cuando necesita los filtros sin observar LiveData.
     */
    fun getCurrentFiltersSnapshot(): InvoiceFilters? {
        return _currentFilters.value
    }
}
