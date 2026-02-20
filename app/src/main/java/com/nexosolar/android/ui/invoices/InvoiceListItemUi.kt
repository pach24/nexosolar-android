package com.nexosolar.android.ui.invoices

import com.nexosolar.android.domain.models.InvoiceState

data class InvoiceListItemUi(
    val id: Int,
    val dateText: String,
    val amountText: String,
    val state: InvoiceState
)
