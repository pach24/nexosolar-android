package com.nexosolar.android.data

import com.nexosolar.android.data.remote.InstallationDTO
import com.nexosolar.android.domain.models.Installation

/**
 * Mapper para convertir InstallationDTO (capa data) a Installation (capa domain).
 *
 * Estrategia:
 * - Mapeo funcional con constructor directo (no .apply)
 * - Manejo de nulls con operador Elvis (?:)
 * - Sin efectos secundarios (función pura)
 */
object InstallationMapper {

    /**
     * Convierte un DTO de instalación en un modelo de dominio inmutable.
     *
     * @param dto DTO recibido desde la API (puede ser null)
     * @return Installation del dominio, o null si dto es null
     */
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
}
