package com.nexosolar.android.ui.invoices.models

import com.nexosolar.android.domain.models.InvoiceState

data class InvoiceListItemUi(
    val invoiceId: Int,
    val state: InvoiceState,
    val formattedDate: String,
    val formattedAmount: String
)

