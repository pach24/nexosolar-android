package com.nexosolar.android.ui.invoices.managers

import com.nexosolar.android.domain.models.Invoice
import java.time.LocalDate


/**
 * Clase para calcular datos derivados de las facturas
 * Ejemplo, importe mínimo, máximo, fecha más antigua, fecha más nueva
 */

class InvoiceStatisticsCalculator {
    fun calculateMaxAmount(invoices: List<Invoice>?): Float {
        if (invoices == null || invoices.isEmpty()) {
            return 0f
        }

        var max = 0f
        for (invoice in invoices) {
            if (invoice.invoiceAmount > max) {
                max = invoice.invoiceAmount
            }
        }
        return max
    }

    fun calculateOldestDate(invoices: List<Invoice>?): LocalDate? {
        if (invoices == null || invoices.isEmpty()) {
            return null
        }

        var oldest: LocalDate? = null
        for (invoice in invoices) {
            val current = invoice.invoiceDate
            if (current != null) {
                if (oldest == null || current.isBefore(oldest)) {
                    oldest = current
                }
            }
        }
        return oldest
    }

    fun calculateNewestDate(invoices: List<Invoice>?): LocalDate? {
        if (invoices == null || invoices.isEmpty()) {
            return null
        }

        var newest: LocalDate? = null
        for (invoice in invoices) {
            val current = invoice.invoiceDate
            if (current != null) {
                if (newest == null || current.isAfter(newest)) {
                    newest = current
                }
            }
        }
        return newest
    }
}