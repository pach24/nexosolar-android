// domain/models/InvoiceFilters.kt
package com.nexosolar.android.domain.models

import java.time.LocalDate

/**
 * Filtros para consultar facturas.
 * Todos los criterios se aplican con lógica AND.
 */
data class InvoiceFilters(
    val minAmount: Float? = null,
    val maxAmount: Float? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val filteredStates: Set<String> = emptySet()
) {
    /**
     * Normaliza los filtros aplicando reglas de negocio.
     *
     * Reglas aplicadas:
     * - Si startDate > endDate, se intercambian
     * - Si minAmount > maxAmount, se intercambian
     *
     * @return Copia corregida de los filtros
     */
    fun normalize(): InvoiceFilters {
        var normalized = this

        // Regla: fecha inicio debe ser anterior o igual a fecha fin
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            normalized = normalized.copy(
                startDate = endDate,
                endDate = startDate
            )
        }

        // Regla: importe mínimo debe ser menor o igual al máximo
        val min = minAmount ?: 0f
        val max = maxAmount ?: Float.MAX_VALUE

        if (min > max) {
            normalized = normalized.copy(
                minAmount = maxAmount,
                maxAmount = minAmount
            )
        }

        return normalized
    }
}
