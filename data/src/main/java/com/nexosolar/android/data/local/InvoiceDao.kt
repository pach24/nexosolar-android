package com.nexosolar.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    // Único punto de lectura: Flow reactivo
    @Query("SELECT * FROM facturas")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    // Helper ligero para verificar si hay datos sin cargar toda la lista
    @Query("SELECT COUNT(*) FROM facturas")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facturas: List<InvoiceEntity>)

    @Query("DELETE FROM facturas")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceInvoices(invoices: List<InvoiceEntity>) {
        deleteAll()
        insertAll(invoices)
    }


}

