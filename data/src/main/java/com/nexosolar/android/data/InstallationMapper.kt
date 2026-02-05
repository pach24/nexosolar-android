package com.nexosolar.android.data

import com.nexosolar.android.data.remote.InstallationDTO
import com.nexosolar.android.domain.models.Installation

/**
 * Mapper para convertir InstallationDTO (capa data) a Installation (capa domain)
 * y aislar el dominio de los nombres de atributos del JSON/API.
 */
object InstallationMapper {

    /**
     * Convierte un DTO de instalación en un modelo de dominio.
     *
     * @param dto DTO recibido desde la API
     * @return Installation del dominio, o null si dto es null
     */
    fun toDomain(dto: InstallationDTO?): Installation? {
        if (dto == null) return null

        return Installation().apply {
            selfConsumptionCode = dto.cau
            installationStatus = dto.status
            installationType = dto.type
            compensation = dto.compensation
            power = dto.power
        }
    }
}
