package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InstallationMapper
import com.nexosolar.android.data.source.InstallationLocalDataSource
import com.nexosolar.android.data.source.InstallationRemoteDataSource
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de instalaciones usando Flow y Offline-First.
 *
 * Sigue la misma arquitectura que InvoiceRepository:
 * 1. Emite datos locales inmediatamente (Room).
 * 2. Actualiza en segundo plano desde la red (onStart).
 * 3. Gestiona errores de red sin romper la UI si hay caché.
 */
class InstallationRepositoryImpl(
    private val remoteDataSource: InstallationRemoteDataSource,
    private val localDataSource: InstallationLocalDataSource,
    private val mapper: InstallationMapper = InstallationMapper
) : InstallationRepository {

    private companion object {
        private const val TAG = "InstallationRepo"
    }

    /**
     * Obtiene los detalles de la instalación como un flujo reactivo.
     * Retorna Flow<Installation?> porque podría no haber datos aún.
     */
    override fun getInstallationDetails(): Flow<Installation?> {
        return localDataSource.getInstallations()
            .map { entities ->
                // Tomamos el primero de la lista (asumiendo 1 instalación por usuario)
                entities.firstOrNull()?.let { mapper.toDomain(it) }
            }
            .onStart {
                try {
                    fetchAndCacheInstallation()
                } catch (e: Exception) {
                    Logger.e(TAG, "[NETWORK] Background update failed: ${e.message}")

                    if (localDataSource.isCacheEmpty()) {
                        // Si no hay caché y falla la red, propagamos el error
                        throw e
                    } else {
                        Logger.w(TAG, "Using cached installation data")
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
     * Fuerza la recarga de datos.
     */
    override suspend fun refreshInstallation(){
        withContext(Dispatchers.IO){
            try {
                val dto = remoteDataSource.getInstallation()
                val entity = mapper.toEntity(dto)

                // Guardamos (como lista de 1 elemento)
                entity?.let { localDataSource.replaceInstallations(listOf(it)) }
            } catch (e: Exception) {
                Logger.e(TAG, "[REFRESH] Error: ${e.message}")
                throw e
            }
        }

    }

    private suspend fun fetchAndCacheInstallation() {
        Logger.d(TAG, "[NETWORK] Fetching installation...")
        val dto = remoteDataSource.getInstallation()

        // Mapeamos a Entidad de BD
        val entity = mapper.toEntity(dto)

        if (entity != null) {
            localDataSource.replaceInstallations(listOf(entity))
            Logger.d(TAG, "[DATABASE] Saved installation -> Flow updated")
        } else {
            Logger.e(TAG, "[ERROR] Mapping failed or null data")
        }
    }
}
