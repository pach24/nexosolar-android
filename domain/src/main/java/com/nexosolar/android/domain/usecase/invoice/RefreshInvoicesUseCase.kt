// File: domain/usecase/invoice/RefreshInvoicesUseCase.kt
package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.repository.InvoiceRepository
import javax.inject.Inject

class RefreshInvoicesUseCase @Inject constructor (
    private val repository: InvoiceRepository
) {

    suspend operator fun invoke() {
        repository.refreshInvoices()
    }
}
