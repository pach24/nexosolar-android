package com.nexosolar.android.domain.models

import java.time.LocalDate

/**
 * Clase de datos que encapsula todos los criterios de filtrado de facturas.
 * Sigue el patrón MVVM manteniendo la lógica de validación separada de la UI.
 * Centraliza el estado de los filtros para facilitar la comunicación entre ViewModel y UI
 */
data class InvoiceFilters(
    var filteredStates: MutableList<String> = ArrayList(),
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = LocalDate.now(),
    var minAmount: Double? = 0.0,
    var maxAmount: Double? = 0.0
)
