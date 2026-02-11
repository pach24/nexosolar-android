
package com.nexosolar.android.ui.invoices

import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.domain.models.InvoiceFilters

/**
 * Estado unificado de UI para los filtros de facturas.
 *
 * Encapsula:
 * - Filtros actuales del usuario
 * - Estadísticas calculadas (rangos de fechas e importes)
 * - Estado de aplicación (loading al aplicar filtros)
 */
data class InvoiceFilterUIState(
    val filters: InvoiceFilters = InvoiceFilters(),
    val statistics: FilterStatistics = FilterStatistics(),
    val isApplying: Boolean = false
) {
    /**
     * Estadísticas derivadas de las facturas cargadas.
     * Usadas para configurar límites de sliders y date pickers.
     */
    data class FilterStatistics(
        val maxAmount: Float = 0f,
        val oldestDateMillis: Long = 0L,
        val newestDateMillis: Long = 0L
    )

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Obtiene las constraints para el DatePicker de fecha inicio.
     * Limita la selección entre la fecha más antigua y la fecha fin (si existe).
     *
     * @return Par (minMillis, maxMillis) para validación del calendario
     */
    fun getStartDateConstraints(): Pair<Long, Long> {
        val maxConstraint = filters.endDate?.let {
            DateUtils.toEpochMilli(it)
        } ?: statistics.newestDateMillis

        return statistics.oldestDateMillis to maxConstraint
    }

    /**
     * Obtiene las constraints para el DatePicker de fecha fin.
     * Limita la selección entre la fecha inicio (si existe) y la fecha más reciente.
     *
     * @return Par (minMillis, maxMillis) para validación del calendario
     */
    fun getEndDateConstraints(): Pair<Long, Long> {
        val minConstraint = filters.startDate?.let {
            DateUtils.toEpochMilli(it)
        } ?: statistics.oldestDateMillis

        return minConstraint to statistics.newestDateMillis
    }

    /**
     * Obtiene la fecha inicial para el DatePicker de fecha inicio.
     *
     * @return Milisegundos de la fecha a preseleccionar
     */
    fun getStartDateInitialSelection(): Long {
        return DateUtils.toEpochMilli(filters.startDate)
            .takeIf { it != 0L }
            ?: statistics.oldestDateMillis
    }

    /**
     * Obtiene la fecha inicial para el DatePicker de fecha fin.
     *
     * @return Milisegundos de la fecha a preseleccionar
     */
    fun getEndDateInitialSelection(): Long {
        return DateUtils.toEpochMilli(filters.endDate)
            .takeIf { it != 0L }
            ?: statistics.newestDateMillis
    }
}
