package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.source.InvoiceLocalDataSource
import com.nexosolar.android.data.source.InvoiceRemoteDataSource
import com.nexosolar.android.core.Logger
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
import javax.inject.Inject

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
class InvoiceRepositoryImpl @Inject constructor(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val localDataSource: InvoiceLocalDataSource,
    private val mapper: InvoiceMapper,
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


        // Modo Producción (Pure Flow)
        return localDataSource.getAllInvoices() // Fuente de verdad única
            .map { entities ->
                Logger.d("InvoiceRepo", "Emitting ${entities.size} invoices from Room (Cache)")
                mapper.toDomainList(entities)
            }
            .onStart {
                // Actualización lateral (side-effect)
                // Si falla la red, capturamos aquí para NO romper el Flow de Room
                Logger.d("InvoiceRepo", "Triggering network fetch...")
                try {
                    fetchAndCacheInvoices()
                    Logger.d("InvoiceRepo", "Network fetch success. Data saved to Room.")
                } catch (e: Exception) {
                    Logger.e("InvoiceRepo", "Network fetch failed: ${e.message}")
                    val isCacheEmpty = localDataSource.isCacheEmpty()

                    if (isCacheEmpty) {
                        // No hay datos locales, lanzamos el error
                        throw e
                    } else {
                        // Hay datos locales, ignoramos el error
                        Logger.w(TAG, "Network failed but cache exists. Showing offline data.")
                    }
                }
            }
            .catch { e ->
                Logger.e(TAG, "[FLOW] Error: ${e.message}")
                throw e
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Fuerza actualización manual (Pull-to-Refresh).
     * Limpia la BD y trae datos nuevos. Room emitirá automáticamente al guardar.
     */
    override suspend fun refreshInvoices() = withContext(Dispatchers.IO) {
        // SIEMPRE pedimos a la fuente remota (sea Mock o Real) y guardamos en Room
        val entities = remoteDataSource.getFacturas()
        saveToDatabase(entities)
    }

    // =========================================================================
    // MÉTODOS PRIVADOS
    // =========================================================================

    /**
     * Lógica de actualización automática en segundo plano.
     * Llamado desde onStart{} del Flow.
     */
    private suspend fun fetchAndCacheInvoices() {


        Logger.d(TAG, "[NETWORK] Requesting data from remote source...")

        // Si esto falla, la excepción subirá al onStart (¡EXACTAMENTE LO QUE QUEREMOS!)
        val remoteEntities = remoteDataSource.getFacturas()

        Logger.d(TAG, "[NETWORK] Received ${remoteEntities.size} invoices")

        saveToDatabase(remoteEntities)
        Logger.d(TAG, "[DATABASE] Saved to Room -> Flow updated automatically")
    }

    /**
     * Transacción de reemplazo total en base de datos.
     */
    private suspend fun saveToDatabase(entities: List<InvoiceEntity>) {
        localDataSource.replaceInvoices(entities)
    }
}
