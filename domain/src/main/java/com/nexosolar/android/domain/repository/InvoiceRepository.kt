package com.nexosolar.android.domain.repository

import com.nexosolar.android.domain.models.Invoice

/**
 * Contrato de repositorio para la gestión de facturas.
 * Define las operaciones de lectura y sincronización disponibles para los casos de uso.
 *
 * Usa corrutinas para operaciones asíncronas. El manejo de errores se realiza
 * mediante excepciones que los casos de uso capturan con try-catch.
 */
interface InvoiceRepository {

    /**
     * Obtiene el listado de facturas.
     * La implementación decidirá la estrategia de cache (Single Source of Truth).
     *
     * @return Lista de facturas del usuario
     * @throws Exception si ocurre un error de red o base de datos
     */
    suspend fun getInvoices(forceUpdate: Boolean = false): List<Invoice>

    /**
     * Fuerza una actualización de datos desde la fuente remota.
     *
     * @throws Exception si ocurre un error de red
     */
    suspend fun refreshInvoices()
}
