package com.nexosolar.android.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de la API que contiene el listado de facturas.
 *
 * Mapea la estructura JSON:
 * ```json
 * {
 *   "numFacturas": 10,
 *   "facturas": [...]
 * }
 * ```
 */
data class InvoiceResponse(
    @SerializedName("numFacturas")
    val numFacturas: Int = 0,  // Valor por defecto si falta en JSON

    @SerializedName("facturas")
    val facturas: List<InvoiceDto> = emptyList()  // Inmutable, no nullable
)
