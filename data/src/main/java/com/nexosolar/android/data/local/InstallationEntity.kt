package com.nexosolar.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de base de datos Room que representa una instalación solar.
 *
 * Mapea directamente a la tabla "installation" en la base de datos local.
 * Almacena información técnica y administrativa de la instalación SmartSolar
 * del usuario.
 */
@Entity(tableName = "installation")
data class InstallationEntity(
    @PrimaryKey(autoGenerate = true)
    val idRoom: Int = 0,

    val cau: String = "",
    val status: String = "",
    val type: String = "",
    val compensation: String = "",
    val power: String = ""
)
