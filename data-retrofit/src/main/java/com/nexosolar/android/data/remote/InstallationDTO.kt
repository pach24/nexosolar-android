package com.nexosolar.android.data.remote

import com.google.gson.annotations.SerializedName

/**
 * DTO para representar los detalles de una instalación solar.
 *
 * Mapea los campos de configuración de autoconsumo desde la API.
 */
data class InstallationDTO(
    @SerializedName("cau")
    val cau: String = "",  //  CAU (Código de Autoconsumo Único)

    @SerializedName("estadoAutoconsumo")
    val status: String = "",  //  Estado del autoconsumo

    @SerializedName("tipoAutoconsumo")
    val type: String = "",  //  Tipo de autoconsumo

    @SerializedName("compExcedentes")
    val compensation: String = "",  //  Compensación de excedentes

    @SerializedName("potenciaInstalacion")
    val power: String = ""  //  Potencia instalada
)
