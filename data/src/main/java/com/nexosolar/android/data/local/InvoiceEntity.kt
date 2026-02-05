package com.nexosolar.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entidad de base de datos Room que representa una factura.
 *
 * Mapea directamente a la tabla "facturas" en la base de datos local.
 * Los campos públicos permiten acceso directo desde Room y simplifican
 * el mapeo, siguiendo las convenciones de Room Database.
 *
 * Requiere RoomConverters para manejar el tipo LocalDate.
 */
@Entity(tableName = "facturas")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val importe: Float = 0f,

    val estado: String = "",

    /**
     * Fecha de la factura.
     * Convertida automáticamente mediante RoomConverters (LocalDate <-> Long).
     */
    val fecha: LocalDate? = null
)
