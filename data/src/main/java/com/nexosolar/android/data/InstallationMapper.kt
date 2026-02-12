package com.nexosolar.android.data

import com.nexosolar.android.data.local.InstallationEntity
import com.nexosolar.android.data.remote.InstallationDTO
import com.nexosolar.android.domain.models.Installation

/**
 * Mapper completo para convertir entre capas: Remote (DTO) ↔ Local (Entity) ↔ Domain.
 */
object InstallationMapper {

    // =========================================================================
    // DTO -> DOMAIN (Usado cuando no hay persistencia o test directos)
    // =========================================================================

    fun toDomain(dto: InstallationDTO?): Installation? {
        if (dto == null) return null
        return Installation(
            selfConsumptionCode = dto.cau ?: "",
            installationStatus = dto.status ?: "",
            installationType = dto.type ?: "",
            compensation = dto.compensation ?: "",
            power = dto.power ?: ""
        )
    }

    // =========================================================================
    // ENTITY -> DOMAIN (Usado al leer de Room)
    // =========================================================================

    fun toDomain(entity: InstallationEntity): Installation {
        return Installation(
            selfConsumptionCode = entity.cau,
            installationStatus = entity.status,
            installationType = entity.type,
            compensation = entity.compensation,
            power = entity.power
        )
    }

    // =========================================================================
    // DTO -> ENTITY (Usado al guardar en Room)
    // =========================================================================

    fun toEntity(dto: InstallationDTO?): InstallationEntity? {
        if (dto == null) return null
        return InstallationEntity(
            cau = dto.cau ?: "",
            status = dto.status ?: "",
            type = dto.type ?: "",
            compensation = dto.compensation ?: "",
            power = dto.power ?: ""
        )
    }
}
