package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el listado de facturas.
 *
 * Encapsula la lógica de negocio de carga de facturas,
 * delegando en el repositorio la estrategia de caché.
 */
class GetInvoicesUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {

    /**
     * Ejecuta el caso de uso para obtener facturas.
     *
     * @return Flow que emite listas de facturas cuando hay cambios
     */
    suspend operator fun invoke(): Flow<List<Invoice>>  {
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
