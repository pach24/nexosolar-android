package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import com.nexosolar.android.domain.repository.RepositoryCallback

/**
 * Caso de uso encargado de obtener el listado de facturas.
 * Proporciona al ViewModel la lista de facturas
 */
class GetInvoicesUseCase(private val repository: InvoiceRepository) {
    operator fun invoke(callback: RepositoryCallback<List<Invoice?>?>?) {
        repository.getInvoices(callback)
    }

    fun refresh(callback: RepositoryCallback<Boolean?>?) {
        repository.refreshInvoices(callback)
    }
}
