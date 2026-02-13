package com.nexosolar.android.data.source

import com.nexosolar.android.data.local.InvoiceEntity
import kotlinx.coroutines.flow.Flow

interface InvoiceLocalDataSource {
    fun getAllInvoices(): Flow<List<InvoiceEntity>>
    suspend fun replaceInvoices(invoices: List<InvoiceEntity>)
    suspend fun isCacheEmpty(): Boolean
    suspend fun deleteAll()

    suspend fun getCount(): Int

    suspend fun insertAll(invoices: List<InvoiceEntity>)
}
