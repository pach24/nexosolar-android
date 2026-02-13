package com.nexosolar.android.domain.models

import java.io.Serializable
import java.time.LocalDate

/**
 * Entidad pura de Dominio que representa una factura.
 * Mantiene la independencia de frameworks (sin anotaciones de Room/Retrofit).
 * Incluye lógica de negocio para transformación de estados.
 */
data class Invoice(
    val invoiceID: Int = 0,
    val invoiceStatus: String? = null,
    val invoiceAmount: Float = 0f,
    val invoiceDate: LocalDate? = null
) : Serializable {

    // ===== Propiedades Computadas (Extensiones de Lógica) =====

    /**
     * Convierte el estado de texto (API) a un Enum de dominio seguro.
     * Facilita la lógica de UI (colores, iconos) evitando comparaciones de strings.
     *
     * @return Enum [InvoiceState] correspondiente o DESCONOCIDO.
     */
    val estadoEnum: InvoiceState
        get() = InvoiceState.fromServerValue(this.invoiceStatus)

    // ===== Nota sobre equals(), hashCode() y toString() =====
    // Al usar 'data class', Kotlin genera automáticamente implementaciones
    // correctas y optimizadas de equals(), hashCode() y toString() basadas
    // en las propiedades del constructor principal.
    // No hace falta escribirlos manualmente salvo que necesites lógica muy específica.
}
