package com.nexosolar.android.domain.models

/**
 * Enum que define los posibles estados de una factura.
 * Actúa como adaptador entre los textos del servidor y la lógica de negocio.
 */
enum class InvoiceState(val serverValue: String) {

    PENDING("Pendiente de pago"),
    PAID("Pagada"),
    CANCELLED("Anulada"),
    FIXED_FEE("Cuota fija"),
    PAYMENT_PLAN("Plan de pago"),
    UNKNOWN(""); // Mantenemos UNKNOWN para robustez (typo original: UNKNONWN)

    companion object {
        /**
         * Mapea un string arbitrario del servidor al enum correspondiente.
         * Case-insensitive para mayor robustez ante cambios en la API.
         */
        fun fromServerValue(value: String?): InvoiceState {
            if (value.isNullOrBlank()) return UNKNOWN

            return entries.firstOrNull {
                it.serverValue.equals(value, ignoreCase = true)
            } ?: UNKNOWN
        }
    }
}
