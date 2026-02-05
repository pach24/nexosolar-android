package com.nexosolar.android.ui.smartsolar.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gestor especializado en el ciclo de vida de los datos de instalación solar.
 */
class InstallationDataManager(private val repository: InstallationRepository) {

    // ===== Variables de instancia =====

    private val _installation = MutableLiveData<Installation?>()

    /**
     * LiveData observable para la UI.
     */
    val installation: LiveData<Installation?> get() = _installation

    private var cachedInstallation: Installation? = null

    // ===== Métodos de operación con Corrutinas =====

    /**
     * Carga los detalles de la instalación de forma asíncrona.
     * @return La instalación cargada.
     * @throws Exception si ocurre un error en el repositorio.
     */
    suspend fun loadInstallationDetails(): Installation {
        // 1. Estrategia de caché: si ya tenemos datos, no vamos a red
        if (hasCachedData()) {
            return cachedInstallation!!
        }

        // 2. Si no hay caché, suspendemos hasta obtener respuesta
        return suspendCancellableCoroutine { continuation ->
            repository.getInstallationDetails(object : InstallationRepository.InstallationCallback {
                override fun onSuccess(installation: Installation) {
                    cachedInstallation = installation
                    _installation.postValue(installation)
                    continuation.resume(installation)
                }

                override fun onError(errorMessage: String) {
                    continuation.resumeWithException(Exception(errorMessage))
                }
            })
        }
    }

    // ===== Métodos de gestión de estado =====

    fun setInstallation(installation: Installation?) {
        _installation.postValue(installation)
        if (installation != null) {
            cachedInstallation = installation
        }
    }

    // ===== Métodos de consulta =====

    /**
     * Verifica si hay datos en memoria.
     */
    fun hasCachedData(): Boolean = cachedInstallation != null

    /**
     * Limpia el LiveData notificando null.
     */
    fun invalidateCache() {
        _installation.postValue(null)
    }

    /**
     * Limpia completamente el estado del manager.
     */
    fun clearAllData() {
        cachedInstallation = null
        _installation.postValue(null)
    }
}