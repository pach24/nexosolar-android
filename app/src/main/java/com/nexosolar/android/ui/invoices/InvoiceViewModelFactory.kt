package com.nexosolar.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.RefreshInvoicesUseCase

/**
 * Factory para instanciar InvoiceViewModel con sus dependencias.
 * El uso de modelClass.isAssignableFrom garantiza que solo se cree el ViewModel correcto.
 */
class InvoiceViewModelFactory(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase,
    private val refreshInvoicesUseCase: RefreshInvoicesUseCase
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) -> {
                InvoiceViewModel(getInvoicesUseCase, filterInvoicesUseCase, refreshInvoicesUseCase) as T
            }
            else -> throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}