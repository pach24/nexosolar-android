package com.nexosolar.android.domain.models

import java.io.Serializable
import java.time.LocalDate

/**
 * Encapsula los criterios de filtrado.
 * Data class proporciona automáticamente equals, hashCode, toString y copy.
 */
data class InvoiceFilters(
    // Valores por defecto definen el estado inicial "limpio"
    val filteredStates: Set<String> = emptySet(), // Set es mejor para búsquedas rápidas que List
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = LocalDate.now(), // Por defecto hasta hoy
    val minAmount: Float? = 0f,   // Alineado a Float como Invoice
    val maxAmount: Float? = 0f
) : Serializable
