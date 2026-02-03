package com.nexosolar.android.domain.models

import java.io.Serializable
import java.time.LocalDate

/**
 * Entidad pura de Dominio que representa una factura.
 * Mantiene la independencia de frameworks (sin anotaciones de Room/Retrofit).
 * Incluye lógica de negocio para transformación de estados.
 */
data class Invoice(
    var invoiceID: Int = 0,
    var invoiceStatus: String? = null,
    var invoiceAmount: Float = 0f,
    var invoiceDate: LocalDate? = null
) : Serializable {

    // Constructor secundario para compatibilidad con código Java legacy que usa new Invoice(status, amount, date)
    constructor(descEstado: String, amountOrder: Float, invoiceDate: LocalDate) : this(
        invoiceID = 0,
        invoiceStatus = descEstado,
        invoiceAmount = amountOrder,
        invoiceDate = invoiceDate
    )

    /**
     * Convierte el estado de texto (API) a un Enum de dominio seguro.
     * Facilita la lógica de UI (colores, iconos) evitando comparaciones de strings.
     *
     * @return Enum [InvoiceState] correspondiente o DESCONOCIDO.
     */
    val estadoEnum: InvoiceState
        get() = InvoiceState.fromServerValue(invoiceStatus)
}
