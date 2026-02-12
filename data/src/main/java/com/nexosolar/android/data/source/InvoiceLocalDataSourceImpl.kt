package com.nexosolar.android.data.source

import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.local.InvoiceEntity
import kotlinx.coroutines.flow.Flow

class InvoiceLocalDataSourceImpl(
    private val dao: InvoiceDao
) : InvoiceLocalDataSource {

    override fun getAllInvoices(): Flow<List<InvoiceEntity>> {
        return dao.getAllInvoices()
    }

    override suspend fun replaceInvoices(invoices: List<InvoiceEntity>) {
        dao.deleteAll()
        dao.insertAll(invoices)
    }

    override suspend fun isCacheEmpty(): Boolean {
        return dao.getCount() == 0
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getCount(): Int {
        return dao.getCount()
    }

    override suspend fun insertAll(invoices: List<InvoiceEntity>) {
        return dao.insertAll(invoices)
    }
}
