package com.nexosolar.android.domain.models

/**
 * Modelo de dominio que representa una instalación solar.
 *
 * Inmutable (vals) para garantizar thread-safety y compatibilidad con Flow.
 * Data class para aprovechar equals, hashCode, copy y destructuring.
 */
data class Installation(
    val selfConsumptionCode: String = "",
    val installationStatus: String = "",
    val installationType: String = "",
    val compensation: String = "",
    val power: String = ""
)
