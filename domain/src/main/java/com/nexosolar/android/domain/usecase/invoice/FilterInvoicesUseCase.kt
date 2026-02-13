package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para filtrar facturas.
 * Contiene la lógica pura de negocio para aplicar filtros combinados (AND).
 */
class FilterInvoicesUseCase @Inject constructor() {

    /**
     * Aplica filtros combinados (AND) a la lista de facturas.
     * @param invoices Lista original inmutable
     * @param filters Criterios de filtrado encapsulados
     * @return Lista filtrada
     */
    operator fun invoke(
        invoices: List<Invoice>,
        filters: InvoiceFilters
    ): List<Invoice> {
        // Si la lista está vacía, retornamos rápido
        if (invoices.isEmpty()) return emptyList()

        return invoices.filter { invoice ->
            // La factura debe cumplir TODOS los criterios (AND)
            matchesStatus(invoice, filters.filteredStates) &&
                    matchesDateRange(invoice, filters.startDate, filters.endDate) &&
                    matchesAmountRange(invoice, filters.minAmount, filters.maxAmount)
        }
    }

    /**
     * Verifica si la factura cumple con el filtro de estado.
     */
    private fun matchesStatus(invoice: Invoice, allowedStates: Set<String>): Boolean {
        // Si no hay filtro de estado, cualquier estado es válido
        if (allowedStates.isEmpty()) return true

        // Verificamos si el estado de la factura está en los permitidos
        val estadoFactura = invoice.estadoEnum.serverValue
        return estadoFactura in allowedStates
    }

    /**
     * Verifica si la factura cumple con el filtro de fecha.
     */
    private fun matchesDateRange(
        invoice: Invoice,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Boolean {
        val fechaFactura = invoice.invoiceDate ?: return false

        // Si la factura no tiene fecha, no cumple con el filtro

        // Verificar límite inferior
        if (startDate != null && fechaFactura.isBefore(startDate)) return false

        // Verificar límite superior
        if (endDate != null && fechaFactura.isAfter(endDate)) return false

        return true
    }

    /**
     * Verifica si la factura cumple con el filtro de importe.
     */
    private fun matchesAmountRange(
        invoice: Invoice,
        minAmount: Float?,
        maxAmount: Float?
    ): Boolean {
        val importe = invoice.invoiceAmount
        val min = minAmount ?: 0f
        val max = maxAmount ?: Float.MAX_VALUE

        return importe in min..max
    }
}
