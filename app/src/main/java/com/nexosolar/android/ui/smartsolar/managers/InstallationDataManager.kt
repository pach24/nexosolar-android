package com.nexosolar.android.ui.smartsolar.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase

/**
 * Gestor especializado en el ciclo de vida de los datos de instalación solar.
 *
 * Responsabilidades:
 * - Gestionar caché de instalación en memoria
 * - Coordinar carga de datos
 * - Exponer LiveData para observación desde la UI
 */
class InstallationDataManager(
    private val getInstallationDetailsUseCase: GetInstallationDetailsUseCase
) {

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
     *
     * Estrategia: Si hay caché, lo retorna. Si no, consulta el repositorio.
     *
     * @return La instalación cargada
     * @throws Exception si ocurre un error en el repositorio
     */
    suspend fun loadInstallationDetails(): Installation {
        // 1. Estrategia de caché: si ya tenemos datos, no vamos a red
        cachedInstallation?.let {
            return it
        }

        // 2. Si no hay caché, llamamos al use case
        val installation = getInstallationDetailsUseCase()
        cachedInstallation = installation
        _installation.postValue(installation)
        return installation
    }

    // ===== Métodos de gestión de estado =====

    /**
     * Establece la instalación en el LiveData y actualiza el caché.
     *
     * @param installation Instalación a mostrar, o null para limpiar
     */
    fun setInstallation(installation: Installation?) {
        _installation.postValue(installation)
        if (installation != null) {
            cachedInstallation = installation
        }
    }

    // ===== Métodos de consulta =====

    /**
     * Verifica si hay datos en memoria.
     *
     * @return true si hay instalación cacheada, false en caso contrario
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
