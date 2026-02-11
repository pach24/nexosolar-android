package com.nexosolar.android.domain.models


import java.time.LocalDate

/**
 * Obtiene el importe máximo de la lista de facturas.
 * @return Importe máximo o 0f si la lista está vacía
 */
fun List<Invoice>.maxAmount(): Float {
    return this.maxOfOrNull { it.invoiceAmount } ?: 0f
}

/**
 * Obtiene el importe mínimo de la lista de facturas.
 * @return Importe mínimo o 0f si la lista está vacía
 */
fun List<Invoice>.minAmount(): Float {
    return this.minOfOrNull { it.invoiceAmount } ?: 0f
}

/**
 * Obtiene la fecha más antigua de las facturas.
 * @return LocalDate más antiguo o null si no hay fechas
 */
fun List<Invoice>.oldestDate(): LocalDate? {
    return this.mapNotNull { it.invoiceDate }.minOrNull()
}

/**
 * Obtiene la fecha más reciente de las facturas.
 * @return LocalDate más reciente o null si no hay fechas
 */
fun List<Invoice>.newestDate(): LocalDate? {
    return this.mapNotNull { it.invoiceDate }.maxOrNull()
}
