package com.nexosolar.android.data.repository

import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.source.InvoiceRemoteDataSource
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de facturas.
 *
 * Responsabilidades:
 * - Coordinar fuentes de datos remota (API) y local (Room)
 * - Implementar estrategia de caché con Single Source of Truth
 * - Proporcionar fallback a datos antiguos en caso de error de red
 * - Soportar modo Mock para testing sin backend
 *
 * Estrategia de datos:
 * - En modo Mock: bypass de Room, usa MockCircular directamente
 * - En modo Normal: Room como caché, red como fuente principal
 * - Fallback: si falla la red, retorna datos antiguos de Room
 */
class InvoiceRepositoryImpl(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val localDataSource: InvoiceDao,
    private val mapper: InvoiceMapper = InvoiceMapper,
    private val isMockMode: Boolean = false
) : InvoiceRepository {

    private companion object {
        private const val TAG = "InvoiceRepo"
    }

    /**
     * Obtiene la lista de facturas según la estrategia de caché.
     *
     * @param forceUpdate true para forzar actualización desde red (pull-to-refresh)
     * @return Lista de facturas del dominio
     * @throws Exception si no hay red ni datos en caché
     */
    override suspend fun getInvoices(forceUpdate: Boolean): List<Invoice> = withContext(Dispatchers.IO) {
        Logger.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Logger.d(TAG, "[START] getInvoices(forceUpdate=$forceUpdate, mockMode=$isMockMode)")

        // MODO MOCK: Bypass de Room para testing con MockCircular
        if (isMockMode) {
            Logger.d(TAG, "[MOCK] Bypassing Room, using MockCircular")

            return@withContext try {
                val remoteEntities = remoteDataSource.getFacturas()
                Logger.d(TAG, "[MOCK] Received ${remoteEntities.size} invoices from MockCircular")
                mapper.toDomainList(remoteEntities)
            } catch (e: Exception) {
                Logger.e(TAG, "[MOCK] Error: ${e.message}", e)
                throw e
            }
        }

        // MODO PRODUCCIÓN: Estrategia con Room como caché

        // 1. Si no se fuerza actualización, intentar leer de caché
        if (!forceUpdate) {
            val localData = localDataSource.getAllList()
            Logger.d(TAG, "[CACHE] Found ${localData.size} invoices in Room")

            if (localData.isNotEmpty()) {
                Logger.d(TAG, "[CACHE] Returning cached data")
                return@withContext mapper.toDomainList(localData)
            }
            Logger.d(TAG, "[CACHE] Empty, fetching from network...")
        } else {
            Logger.d(TAG, "[REFRESH] Force update requested, skipping cache")
        }

        // 2. Intentar obtener datos de red
        try {
            Logger.d(TAG, "[NETWORK] Requesting data from remote source...")
            val remoteEntities = remoteDataSource.getFacturas()
            Logger.d(TAG, "[NETWORK] Received ${remoteEntities.size} invoices")

            // 3. Guardar en Room para futuras consultas
            saveToDatabase(remoteEntities)
            Logger.d(TAG, "[DATABASE] Saved to Room successfully")

            return@withContext mapper.toDomainList(remoteEntities)

        } catch (e: Exception) {
            Logger.e(TAG, "[ERROR] Network failure: ${e.javaClass.simpleName} - ${e.message}", e)

            // 4. Fallback: intentar usar datos antiguos de Room
            val localData = localDataSource.getAllList()

            if (localData.isNotEmpty()) {
                Logger.w(TAG, "[FALLBACK] Using stale cache (${localData.size} invoices)")
                return@withContext mapper.toDomainList(localData)
            }

            // 5. Sin red ni caché: propagar excepción
            Logger.e(TAG, "[FATAL] No network or cache available")
            throw e
        }
    }

    /**
     * Fuerza la actualización de facturas desde la red.
     * Limpia y reemplaza los datos en Room.
     *
     * @throws Exception si hay error de red
     */
    override suspend fun refreshInvoices() = withContext(Dispatchers.IO) {
        Logger.d(TAG, "[REFRESH] Force refreshing invoices...")
        val entities = remoteDataSource.getFacturas()
        saveToDatabase(entities)
        Logger.d(TAG, "[REFRESH] Completed successfully")
    }

    /**
     * Guarda facturas en la base de datos local.
     * Implementa estrategia de reemplazo total (delete + insert).
     *
     * @param entities Lista de entidades a guardar
     */
    private suspend fun saveToDatabase(entities: List<InvoiceEntity>) {
        localDataSource.deleteAll()
        localDataSource.insertAll(entities)
    }
}
