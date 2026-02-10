package com.nexosolar.android.data.remote

import com.google.gson.annotations.SerializedName

/**
 * DTO para representar una factura desde la API.
 *
 * Mapea los campos de la respuesta JSON a propiedades Kotlin inmutables.
 */
data class InvoiceDto(
    @SerializedName("descEstado")
    val status: String,  // ⚡ val, no var

    @SerializedName("importeOrdenacion")
    val amount: Float,

    @SerializedName("fecha")
    val date: String  // ⚡ No nullable si la API siempre lo envía
)
