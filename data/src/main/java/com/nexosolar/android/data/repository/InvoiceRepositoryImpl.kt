package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.source.InvoiceRemoteDataSource
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de facturas con corrutinas.
 *
 * Responsabilidades:
 * - Gestionar la estrategia de caché (local vs remoto)
 * - Coordinar entre fuente de datos remota (API) y local (Room)
 * - Aplicar lógica de Single Source of Truth
 * - Transformar datos usando InvoiceMapper
 *
 * Estrategia de datos:
 * - Si alwaysReload=true: siempre consulta red primero
 * - Si alwaysReload=false: devuelve caché si existe, sino red
 * - En caso de error de red con caché disponible: devuelve caché
 */
class InvoiceRepositoryImpl(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val localDataSource: InvoiceDao,
    private val alwaysReload: Boolean
) : InvoiceRepository {

    private val mapper = InvoiceMapper
    // ❌ ELIMINAR: private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override suspend fun getInvoices(): List<Invoice> = withContext(Dispatchers.IO) {
        val localData = localDataSource.getAllList()
        val hasData = localData.isNotEmpty()

        if (alwaysReload || !hasData) {
            fetchFromNetwork(localData)
        } else {
            mapper.toDomainList(localData)
        }
    }

    override suspend fun refreshInvoices() = withContext(Dispatchers.IO) {
        val entities = remoteDataSource.getFacturas()
        saveToDatabase(entities)
    }

    /**
     * Obtiene facturas desde la red y gestiona el fallback a caché.
     *
     * @param localCache Caché local disponible para fallback
     * @return Lista de facturas del dominio
     */
    private suspend fun fetchFromNetwork(localCache: List<InvoiceEntity>): List<Invoice> {
        return try {
            val entities = remoteDataSource.getFacturas()
            saveToDatabase(entities)
            mapper.toDomainList(entities)
        } catch (e: Exception) {
            if (localCache.isNotEmpty()) {
                mapper.toDomainList(localCache)
            } else {
                throw e
            }
        }
    }

    /**
     * Guarda facturas en base de datos local usando estrategia de reemplazo total.
     * Borra todo el contenido previo antes de insertar.
     *
     * @param entities Lista de entidades a guardar
     */
    private suspend fun saveToDatabase(entities: List<InvoiceEntity>) {
        localDataSource.deleteAll()
        localDataSource.insertAll(entities)
    }
}
