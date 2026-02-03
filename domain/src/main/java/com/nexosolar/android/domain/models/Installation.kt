package com.nexosolar.android.domain.models

/**
 * Modelo de dominio que representa una instalación fotovoltaica.
 * Contiene los datos técnicos y administrativos básicos devueltos por la API.
 * Se utiliza principalmente en la pantalla de Dashboard/Home.
 */
data class Installation(
    var selfConsumptionCode: String? = null,
    var installationStatus: String? = null,
    var installationType: String? = null,
    var compensation: String? = null,
    var power: String? = null
) {
    // Constructor secundario para compatibilidad Java si es necesario,
    // aunque el constructor por defecto con argumentos nombrados suele ser suficiente.
    // Al definir valores por defecto (null), Kotlin genera automáticamente el constructor vacío.
}
