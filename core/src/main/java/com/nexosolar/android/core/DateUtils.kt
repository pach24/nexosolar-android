package com.nexosolar.android.core

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilidades para operaciones con fechas en la aplicación.
 *
 * Proporciona formateo de fechas y conversiones entre [LocalDate] y milisegundos (timestamp)
 * para componentes nativos de Android como MaterialDatePicker.
 *
 * Todos los métodos son null-safe.
 */
object DateUtils {

    private val SHORT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault())
    private val READABLE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"))

    /**
     * Formatea una fecha en formato corto: `dd/MM/yy`
     *
     * @param date Fecha a formatear
     * @return Fecha formateada o cadena vacía si [date] es null
     */
    fun formatDateShort(date: LocalDate?): String =
        date?.format(SHORT_FORMATTER).orEmpty()

    /**
     * Formatea una fecha en formato legible: `"20 Ene 2025"`
     *
     * Aplica locale español (es-ES) y capitaliza la primera letra del mes.
     *
     * @param date Fecha a formatear
     * @return Fecha formateada o cadena vacía si [date] es null
     */
    fun formatDate(date: LocalDate?): String =
        date?.format(READABLE_FORMATTER)
            ?.capitalizeMonth()
            ?.replace(".", "")
            .orEmpty()

    /**
     * Convierte un [LocalDate] a milisegundos en UTC.
     *
     * Necesario para configurar MaterialDatePicker.
     *
     * @param date Fecha a convertir
     * @return Milisegundos desde epoch o 0 si [date] es null
     */
    fun toEpochMilli(date: LocalDate?): Long =
        date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: 0L

    /**
     * Capitaliza la primera letra del mes en una fecha formateada.
     *
     * Ejemplo: "20 ene 2025" → "20 Ene 2025"
     */
    private fun String.capitalizeMonth(): String {
        val firstSpaceIndex = indexOf(' ')
        if (firstSpaceIndex == -1 || firstSpaceIndex + 1 >= length) return this

        val monthStartIndex = firstSpaceIndex + 1
        val monthChar = this[monthStartIndex]

        return if (monthChar.isLowerCase()) {
            replaceRange(monthStartIndex..monthStartIndex, monthChar.uppercase())
        } else {
            this
        }
    }
}
