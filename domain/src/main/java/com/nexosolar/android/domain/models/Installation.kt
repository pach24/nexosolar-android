package com.nexosolar.android.domain.models

/**
 * Modelo de dominio que representa una instalación fotovoltaica.
 * Contiene los datos técnicos y administrativos básicos devueltos por la API.
 * Se utiliza principalmente en la pantalla de Dashboard/Home.
 */
class Installation {
    // ===== Getters y Setters =====
    // ===== Variables de instancia =====
    var selfConsumptionCode: String? = null
    var installationStatus: String? = null
    var installationType: String? = null
    var compensation: String? = null
    var power: String? = null

    // ===== Constructores =====
    /**
     * Constructor vacío requerido para serialización (Gson/Retrofit).
     */
    constructor()

    constructor(
        cau: String?,
        status: String?,
        type: String?,
        compensation: String?,
        power: String?
    ) {
        this.selfConsumptionCode = cau
        this.installationStatus = status
        this.installationType = type
        this.compensation = compensation
        this.power = power
    }
}
