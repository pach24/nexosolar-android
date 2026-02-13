package com.nexosolar.android.data

import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.remote.InvoiceDto
import com.nexosolar.android.domain.models.Invoice
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Mapper bidireccional entre modelos de dominio y entidades de base de datos.
 *
 * Responsabilidades:
 * - Transformar InvoiceEntity (capa data) ↔ Invoice (capa domain)
 * - Transformar InvoiceDto (red) → InvoiceEntity (persistencia)
 * - Aislar el dominio de detalles de persistencia y formato de red
 * - Manejo robusto de errores de parsing (fechas inválidas)
 *
 * Estrategia:
 * - Mapeo funcional con constructores directos (inmutable)
 * - Operador Elvis (?:) para valores por defecto
 * - Funciones de extensión para transformaciones de listas
 */
class InvoiceMapper @Inject constructor() {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ========== ENTITY → DOMAIN ==========

    /**
     * Convierte una entidad de base de datos a modelo de dominio.
     *
     * @param entity Entidad de Room, o null
     * @return Modelo de dominio, o null si entity es null
     */
    fun toDomain(entity: InvoiceEntity?): Invoice? {
        if (entity == null) return null

        return Invoice(
            invoiceID = entity.id,
            invoiceStatus = entity.estado,
            invoiceAmount = entity.importe,
            invoiceDate = entity.fecha
        )
    }

    /**
     * Convierte una lista de entidades a lista de modelos de dominio.
     *
     * @param entities Lista de entidades de Room
     * @return Lista de modelos de dominio (vacía si entities es null o vacía)
     */
    fun toDomainList(entities: List<InvoiceEntity>?): List<Invoice> {
        return entities?.mapNotNull { toDomain(it) } ?: emptyList()
    }

    // ========== DOMAIN → ENTITY ==========

    /**
     * Convierte un modelo de dominio a entidad de base de datos.
     *
     * Room genera el ID automáticamente si es autoincrement.
     * Si necesitas preservar el ID original del servidor, descomenta el campo.
     *
     * @param invoice Modelo de dominio Invoice
     * @return Entidad de Room, o null si invoice es null
     */
    fun toEntity(invoice: Invoice?): InvoiceEntity? {
        if (invoice == null) return null

        return InvoiceEntity(
            id = invoice.invoiceID,           // ← Si Room usa @PrimaryKey(autoGenerate = true), omite esto
            estado = invoice.invoiceStatus ?: "",
            importe = invoice.invoiceAmount,
            fecha = invoice.invoiceDate
        )
    }

    // ========== DTO (Red) → ENTITY ==========

    /**
     * Convierte un DTO de red a entidad de base de datos.
     * Parsea la fecha del formato "dd/MM/yyyy" a LocalDate.
     *
     * Manejo de errores:
     * - Si la fecha es inválida, usa null (permite guardar sin fecha)
     * - Alternativa: podrías usar LocalDate.now() como fallback
     *
     * @param dto DTO desde la API
     * @return Entidad de Room, o null si el DTO es null
     */
    fun toEntityFromDto(dto: InvoiceDto?): InvoiceEntity? {
        if (dto == null) return null

        return InvoiceEntity(
            // id se autogenera en Room, no viene del DTO
            estado = dto.status ?: "",
            importe = dto.amount,
            fecha = parseDate(dto.date)
        )
    }

    /**
     * Convierte una lista de DTOs de red a entidades de base de datos.
     *
     * @param dtos Lista de InvoiceDto desde la API
     * @return Lista de entidades Room (descarta DTOs inválidos con mapNotNull)
     */
    fun toEntityListFromDto(dtos: List<InvoiceDto>?): List<InvoiceEntity> {
        return dtos?.mapNotNull { toEntityFromDto(it) } ?: emptyList()
    }

    // ========== DTO (Red) → DOMAIN (DIRECTO - OPCIONAL) ==========

    /**
     * Convierte directamente un DTO de red a modelo de dominio.
     * Útil si no necesitas pasar por cache antes de mostrar en UI.
     *
     * @param dto DTO desde la API
     * @return Modelo de dominio, o null si dto es null
     */
    fun toDomainFromDto(dto: InvoiceDto?): Invoice? {
        if (dto == null) return null

        return Invoice(
            invoiceID = 0, // La API probablemente no envía ID, se genera en Room
            invoiceStatus = dto.status,
            invoiceAmount = dto.amount,
            invoiceDate = parseDate(dto.date)
        )
    }

    /**
     * Convierte lista de DTOs directamente a lista de modelos de dominio.
     *
     * @param dtos Lista de DTOs desde la API
     * @return Lista de modelos de dominio (vacía si dtos es null)
     */
    fun toDomainListFromDto(dtos: List<InvoiceDto>?): List<Invoice> {
        return dtos?.mapNotNull { toDomainFromDto(it) } ?: emptyList()
    }

    // ========== HELPERS PRIVADOS ==========

    /**
     * Parsea una fecha en formato "dd/MM/yyyy" a LocalDate.
     * Manejo robusto de errores: devuelve null si el formato es inválido.
     *
     * @param dateString Fecha como String en formato "dd/MM/yyyy"
     * @return LocalDate parseada, o null si es inválida
     */
    private fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null

        return try {
            LocalDate.parse(dateString, DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            // Log del error (opcional, si usas Logger)
            // Logger.e("InvoiceMapper", "Invalid date format: $dateString", e)
            null
        }
    }
}
