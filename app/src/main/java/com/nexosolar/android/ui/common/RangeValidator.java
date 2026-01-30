package com.nexosolar.android.ui.common;



import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.material.datepicker.CalendarConstraints;
import com.nexosolar.android.core.DateValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Adaptador de UI para MaterialDatePicker.
 * Responsabilidad: Convertir milisegundos de la UI a LocalDate
 * y delegar la validación a la lógica de dominio (DateValidator).
 */
public class RangeValidator implements CalendarConstraints.DateValidator {

    private final long minDateMillis;
    private final long maxDateMillis;

    public RangeValidator(long min, long max) {
        this.minDateMillis = min;
        this.maxDateMillis = max;
    }

    @Override
    public boolean isValid(long dateMillis) {
        // 1. Convertir datos de UI (long) a Dominio (LocalDate)
        // Usamos ZoneOffset.UTC porque MaterialDatePicker trabaja en UTC
        LocalDate dateToCheck = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        LocalDate min = Instant.ofEpochMilli(minDateMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        LocalDate max = Instant.ofEpochMilli(maxDateMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        // 2. Delegar la lógica a tu clase experta en reglas (DateValidator)
        return DateValidator.isWithinBounds(dateToCheck, min, max);
    }

    // ... Implementación de Parcelable (igual que antes) ...
    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(minDateMillis);
        dest.writeLong(maxDateMillis);
    }

    public static final Parcelable.Creator<RangeValidator> CREATOR = new Parcelable.Creator<RangeValidator>() {
        @Override
        public RangeValidator createFromParcel(Parcel source) {
            return new RangeValidator(source.readLong(), source.readLong());
        }
        @Override
        public RangeValidator[] newArray(int size) {
            return new RangeValidator[size];
        }
    };
}
