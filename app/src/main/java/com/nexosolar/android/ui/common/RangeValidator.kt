package com.nexosolar.android.ui.common

import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints
import com.nexosolar.android.core.DateValidator
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZoneOffset

/**
 * Adaptador de UI para MaterialDatePicker.
 * Responsabilidad: Convertir milisegundos de la UI a LocalDate
 * y delegar la validación a la lógica de dominio (DateValidator).
 */
@Parcelize
class RangeValidator(
    private val minDateMillis: Long,
    private val maxDateMillis: Long
) : CalendarConstraints.DateValidator, Parcelable {

    override fun isValid(dateMillis: Long): Boolean {
        // 1. Convertir datos de UI (long) a Dominio (LocalDate)
        // Usamos ZoneOffset.UTC porque MaterialDatePicker trabaja en UTC
        val dateToCheck = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        val min = Instant.ofEpochMilli(minDateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        val max = Instant.ofEpochMilli(maxDateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        // 2. Delegar la lógica a clase experta en reglas (DateValidator)
        return DateValidator.isWithinBounds(dateToCheck, min, max)
    }
}
