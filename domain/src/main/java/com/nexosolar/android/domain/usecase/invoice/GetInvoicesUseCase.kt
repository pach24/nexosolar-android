package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository

/**
 * Caso de uso para obtener el listado de facturas.
 *
 * Encapsula la lógica de negocio de carga de facturas,
 * delegando en el repositorio la estrategia de caché.
 */
class GetInvoicesUseCase(
    private val repository: InvoiceRepository
) {

    /**
     * Ejecuta el caso de uso para obtener facturas.
     *
     * @return Lista de facturas
     * @throws Exception si ocurre un error
     */
    suspend operator fun invoke(): List<Invoice> {
        return repository.getInvoices()
    }

    /**
     * Fuerza la actualización de facturas desde el servidor.
     *
     * @throws Exception si ocurre un error
     */
    suspend fun refresh() {
        repository.refreshInvoices()
    }
}
