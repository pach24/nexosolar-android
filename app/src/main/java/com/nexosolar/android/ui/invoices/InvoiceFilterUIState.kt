
package com.nexosolar.android.ui.invoices
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
}
