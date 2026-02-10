package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.models.Invoice
import java.time.LocalDate

/**
 * Caso de uso para filtrar facturas.
 * Refactorizado para usar características idiomáticas de Kotlin:
 * - Operador 'invoke' para llamadas directas
 * - Funciones de orden superior de colecciones (filter, all, any)
 * - Null-safety mejorada
 * - Parámetros con valores por defecto
 */
class FilterInvoicesUseCase {

    /**
     * Aplica filtros combinados (AND) a la lista de facturas.
     *
     * @param invoices Lista original (inmutable preferiblemente)
     * @param statusList Lista de estados permitidos. Si es null o vacía, se ignora este filtro.
     * @param dateRange Rango de fechas (inicio, fin).
     * @param amountRange Rango de importes (min, max).
     */
    operator fun invoke(
        invoices: List<Invoice>,
        statusList: List<String>? = null,
        dateRange: ClosedRange<LocalDate>? = null,
        amountRange: ClosedFloatingPointRange<Float>? = null
    ): List<Invoice> {
        // Si la lista está vacía, retornamos rápido
        if (invoices.isEmpty()) return emptyList()

        return invoices.filter { invoice ->
            // Usamos una función local o lógica directa para validar cada criterio.
            // La función 'filter' solo incluirá la factura si TODAS las condiciones son true.

            val matchStatus = statusList.isNullOrEmpty() ||
                    statusList.contains(invoice.invoiceStatus)

            val matchDate = isDateInRange(invoice.invoiceDate, dateRange)

            val matchAmount = isAmountInRange(invoice.invoiceAmount, amountRange)

            // Retornamos true solo si cumple todo (AND implícito)
            matchStatus && matchDate && matchAmount
        }
    }

    // Funciones privadas auxiliares para mantener limpio el 'invoke'

    private fun isDateInRange(date: LocalDate?, range: ClosedRange<LocalDate>?): Boolean {
        // Si no hay rango definido, cualquier fecha es válida
        if (range == null) return true
        // Si hay rango pero la factura no tiene fecha, no cumple
        if (date == null) return false

        // Operador 'in' de Kotlin: verifica si está dentro del rango (inclusive)
        return date in range
    }

    private fun isAmountInRange(amount: Float, range: ClosedFloatingPointRange<Float>?): Boolean {
        if (range == null) return true
        return amount in range
    }
}
