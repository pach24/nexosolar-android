package com.nexosolar.android.data

import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.remote.InvoiceDto
import com.nexosolar.android.domain.models.Invoice
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Mapper bidireccional entre modelos de dominio y entidades de base de datos.
 *
 * Responsabilidades:
 * - Transformar InvoiceEntity (capa data) ↔ Invoice (capa domain)
 * - Aislar el dominio de detalles de persistencia y red
 * - Permitir evolución independiente de los modelos en cada capa
 *
 * Ubicación en Clean Architecture: reside en la capa data porque necesita
 * conocer tanto los modelos de dominio como las entidades de persistencia,
 * y es responsabilidad de data preparar los datos para el dominio.
 */
object InvoiceMapper {

    // ===== Mapeo de Entidad a Dominio =====

    /**
     * Convierte una entidad de base de datos a modelo de dominio.
     *
     * @param entity Entidad de Room, o null
     * @return Modelo de dominio, o null si entity es null
     */
    fun toDomain(entity: InvoiceEntity?): Invoice? {
        if (entity == null) return null

        return Invoice().apply {
            invoiceStatus = entity.estado
            invoiceAmount = entity.importe
            invoiceDate = entity.fecha
            // invoiceID = entity.id // Descomentar si el dominio requiere ID
        }
    }

    /**
     * Convierte una lista de entidades a lista de modelos de dominio.
     *
     * @param entities Lista de entidades de Room
     * @return Lista de modelos de dominio (vacía si entities es null)
     */
    fun toDomainList(entities: List<InvoiceEntity>?): List<Invoice> {
        return entities?.mapNotNull { toDomain(it) } ?: emptyList()
    }

    // ===== Mapeo de Dominio a Entidad =====

    /**
     * Convierte un modelo de dominio a entidad de base de datos.
     *
     * @param invoice Modelo de dominio Invoice
     * @return Entidad de Room
     */
    fun toEntity(invoice: Invoice?): InvoiceEntity? {
        if (invoice == null) return null

        return InvoiceEntity(
            estado = invoice.invoiceStatus ?: "",
            importe = invoice.invoiceAmount,
            fecha = invoice.invoiceDate
        )
    }

    /**
     * Convierte una lista de DTOs de red a entidades de base de datos.
     *
     * @param dtos Lista de InvoiceDto desde la API
     * @return Lista de entidades Room
     */
    fun toEntityListFromDto(dtos: List<InvoiceDto>?): List<InvoiceEntity> {
        return dtos?.mapNotNull { toEntityFromDto(it) } ?: emptyList()
    }

    /**
     * Convierte un DTO de red a entidad de base de datos.
     * Parsea la fecha del formato "dd/MM/yyyy" a LocalDate.
     *
     * @param dto DTO desde la API
     * @return Entidad de Room, o null si el DTO es null
     */
    private fun toEntityFromDto(dto: InvoiceDto?): InvoiceEntity? {
        if (dto == null) return null

        val fecha = try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(dto.date, formatter)
        } catch (e: Exception) {
            // Manejo seguro en caso de error de parsing
            null // o LocalDate.now(), según prefieras
        }

        return InvoiceEntity(
            estado = dto.status ?: "",
            importe = dto.amount,
            fecha = fecha
        )
    }
}
