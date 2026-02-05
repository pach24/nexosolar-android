package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.source.InvoiceRemoteDataSource
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import com.nexosolar.android.domain.repository.RepositoryCallback
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Implementación del repositorio de facturas siguiendo el patrón Repository.
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
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun getInvoices(callback: RepositoryCallback<List<Invoice>>) {
        executor.execute {
            val localData = localDataSource.getAllList()
            val hasData = !localData.isNotEmpty()

            if (alwaysReload || !hasData) {
                fetchFromNetwork(callback, localData)
            } else {
                // Devuelve caché local
                callback.onSuccess(mapper.toDomainList(localData))
            }
        }
    }

    override fun refreshInvoices(callback: RepositoryCallback<Boolean>?) {
        remoteDataSource.getFacturas(object : RepositoryCallback<List<InvoiceEntity>> {
            override fun onSuccess(entities: List<InvoiceEntity>) {
                executor.execute {
                    saveToDatabase(entities)
                    callback?.onSuccess(true)
                }
            }

            override fun onError(error: Throwable) {
                callback?.onError(error)
            }
        })
    }

    /**
     * Obtiene facturas desde la red y gestiona el fallback a caché.
     *
     * @param callback Callback para notificar resultado
     * @param localCache Caché local disponible para fallback
     */
    private fun fetchFromNetwork(
        callback: RepositoryCallback<List<Invoice>>,
        localCache: List<InvoiceEntity>?
    ) {
        remoteDataSource.getFacturas(object : RepositoryCallback<List<InvoiceEntity>> {
            override fun onSuccess(entities: List<InvoiceEntity>) {
                executor.execute {
                    saveToDatabase(entities)
                    callback.onSuccess(mapper.toDomainList(entities))
                }
            }

            override fun onError(error: Throwable) {
                executor.execute {
                    if (!localCache.isNullOrEmpty()) {
                        // Devuelve caché como fallback
                        callback.onSuccess(mapper.toDomainList(localCache))
                    } else {
                        callback.onError(error)
                    }
                }
            }
        })
    }

    /**
     * Guarda facturas en base de datos local usando estrategia de reemplazo total.
     * Borra todo el contenido previo antes de insertar.
     *
     * @param entities Lista de entidades a guardar
     */
    private fun saveToDatabase(entities: List<InvoiceEntity>) {
        localDataSource.deleteAll()
        localDataSource.insertAll(entities)
    }
}
