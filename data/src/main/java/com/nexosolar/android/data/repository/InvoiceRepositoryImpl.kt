package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.source.InvoiceRemoteDataSource
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de facturas usando Flow (Programación Reactiva).
 *
 * Responsabilidades:
 * - Coordinar fuentes de datos remota (API) y local (Room) con patrón Offline-First
 * - Soportar modo Mock para testing sin backend (bypass de Room)
 * - Gestionar errores de red manteniendo caché visible (Fallback automático)
 *
 * Estrategia de datos con Flow:
 * - Modo Mock: Flow directo desde MockCircular (sin persistencia)
 * - Modo Normal:
 *   1. Room emite caché inmediatamente (Flow reactivo)
 *   2. onStart lanza actualización en segundo plano (red)
 *   3. Al guardar en Room, el Flow emite automáticamente los nuevos datos
 *   4. catch maneja errores de red sin romper el flujo de UI
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
     * Obtiene el flujo reactivo de facturas.
     *
     * @return Flow que emite actualizaciones automáticas de facturas
     */
    override fun getInvoices(): Flow<List<Invoice>> {
        // Modo Mock
        if (isMockMode) {
            return flow {
                emit(mapper.toDomainList(remoteDataSource.getFacturas()))
            }.flowOn(Dispatchers.IO)
        }

        // Modo Producción (Pure Flow)
        return localDataSource.getAllInvoices() // Fuente de verdad única
            .map { entities ->
                mapper.toDomainList(entities)
            }
            .onStart {
                // Actualización lateral (side-effect)
                // Si falla la red, capturamos aquí para NO romper el Flow de Room
                try {
                    fetchAndCacheInvoices()
                } catch (e: Exception) {
                    Logger.e(TAG, "[NETWORK] Background update failed: ${e.message}")
                    // No re-lanzamos la excepción: el usuario sigue viendo el caché felizmente
                }
            }
            .catch { e ->
                // Este catch solo captura errores de Room (lectura de BD)
                // Si Room falla, es un error fatal de aplicación
                Logger.e(TAG, "[DATABASE] Room flow failed: ${e.message}")
                throw e
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Fuerza actualización manual (Pull-to-Refresh).
     * Limpia la BD y trae datos nuevos. Room emitirá automáticamente al guardar.
     */
    override suspend fun refreshInvoices() = withContext(Dispatchers.IO) {
        Logger.d(TAG, "[REFRESH] Force refreshing invoices...")

        if (isMockMode) {
            Logger.d(TAG, "[REFRESH] Mock mode: bypassing Room refresh logic")
            return@withContext
        }

        try {
            val entities = remoteDataSource.getFacturas()
            Logger.d(TAG, "[REFRESH] Received ${entities.size} invoices")
            saveToDatabase(entities) // Esto disparará el Flow automáticamente
            Logger.d(TAG, "[REFRESH] Completed successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "[REFRESH] Error: ${e.message}", e)
            throw e
        }
    }

    // =========================================================================
    // MÉTODOS PRIVADOS
    // =========================================================================

    /**
     * Lógica de actualización automática en segundo plano.
     * Llamado desde onStart{} del Flow.
     */
    private suspend fun fetchAndCacheInvoices() {
        try {
            val cacheCount = localDataSource.getCount()

            // Si ya hay caché, no bloqueamos ni forzamos (Room ya emitió los datos)
            // Solo actualizamos si está vacío o si queremos política de "siempre actualizar"
            // Aquí asumimos política: Si caché existe, NO forzar red inmediatamente (ahorro datos)
            // Si prefieres "siempre actualizar al abrir", comenta el if(cacheCount > 0)

            /* Política actual: Siempre intenta actualizar para tener datos frescos */
            Logger.d(TAG, "[NETWORK] Requesting data from remote source...")
            val remoteEntities = remoteDataSource.getFacturas()
            Logger.d(TAG, "[NETWORK] Received ${remoteEntities.size} invoices")

            // Guardar dispara la emisión automática de Room
            saveToDatabase(remoteEntities)
            Logger.d(TAG, "[DATABASE] Saved to Room -> Flow updated automatically")

        } catch (e: Exception) {
            // Error silencioso en background: El usuario ya está viendo el caché
            Logger.e(TAG, "[ERROR] Network background update failed: ${e.message}")
            // No hacemos throw aquí para no romper el Flow que ya muestra datos cacheados
        }
    }

    /**
     * Transacción de reemplazo total en base de datos.
     */
    private suspend fun saveToDatabase(entities: List<InvoiceEntity>) {
        localDataSource.deleteAll()
        localDataSource.insertAll(entities)
    }
}
