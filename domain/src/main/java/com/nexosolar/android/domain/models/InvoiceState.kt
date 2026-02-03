package com.nexosolar.android.domain.models

/**
 * Enum que define los posibles estados de una factura en el sistema.
 * Actúa como adaptador entre los textos del servidor y la lógica de negocio/UI.
 */
enum class InvoiceState(val serverValue: String) {
    PENDING("Pendiente de pago"),
    PAID("Pagada"),
    CANCELLED("Anulada"),
    FIXED_FEE("Cuota fija"),
    PAYMENT_PLAN("Plan de pago"),
    UNKNONWN("");

    companion object {
        /**
         * Mapea un string arbitrario del servidor al enum correspondiente.
         * Case-insensitive para mayor robustez ante cambios menores en API.
         */
        @JvmStatic
        fun fromServerValue(value: String?): InvoiceState {
            if (value == null) return UNKNONWN
            return values().find { it.serverValue.equals(value, ignoreCase = true) } ?: UNKNONWN
        }
    }
}
