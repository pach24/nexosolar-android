package com.nexosolar.android.ui.smartsolar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import com.nexosolar.android.ui.smartsolar.managers.InstallationDataManager
import com.nexosolar.android.ui.smartsolar.managers.InstallationStateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona el estado y la lógica de negocio relacionada
 * con los datos de instalación solar.
 *
 * Responsabilidades:
 * - Coordinar managers especializados (DataManager, StateManager)
 * - Exponer LiveData para observación desde la UI
 * - Gestionar estados de carga y errores con corrutinas
 */
class InstallationViewModel(repository: InstallationRepository) : ViewModel() {

    // ===== Managers =====

    private val dataManager = InstallationDataManager(repository)
    private val stateManager = InstallationStateManager()

    private var isFirstLoad = true

    // ===== LiveData expuestos =====

    val installation: LiveData<Installation?> = dataManager.installation
    val viewState: LiveData<InstallationStateManager.ViewState> = stateManager.currentState
    val errorMessage: LiveData<String?> = stateManager.errorMessage
    val showEmptyError: LiveData<Boolean> = stateManager.showEmptyError

    // ===== Métodos públicos =====

    /**
     * Solicita los detalles de la instalación mediante el DataManager.
     * Actualiza los LiveData según el resultado (éxito/error).
     */
    fun loadInstallationDetails() {
        stateManager.showLoading()

        // Lanzamos una corrutina en el scope del ViewModel
        viewModelScope.launch {
            try {
                val result = dataManager.loadInstallationDetails()
                isFirstLoad = false
                stateManager.showData()

            } catch (e: Exception) {
                // Cualquier error que caiga en onError del Repository caerá aquí automáticamente
                isFirstLoad = false
                handleLoadError(e)
            }
        }
    }

    /**
     * Verifica si hay datos cargados en memoria.
     */
    fun hasCachedData(): Boolean = dataManager.hasCachedData()

    /**
     * Verifica si el estado actual es de error.
     */
    fun isErrorState(): Boolean = stateManager.isError()

    // ===== Métodos privados =====

    private fun handleLoadError(error: Throwable) {
        val errorType = ErrorClassifier.classify(error)

        if (dataManager.hasCachedData()) {
            stateManager.showData()
            return
        }

        val message = ErrorClassifier.getErrorMessage(errorType, error)
        when (errorType) {
            ErrorClassifier.ErrorType.SERVER -> stateManager.showServerError(message)
            ErrorClassifier.ErrorType.NETWORK -> {
                viewModelScope.launch {
                    delay(3000)
                    stateManager.showNetworkError(message)
                }
            }
            else -> stateManager.showServerError("Error inesperado: $message")
        }
    }
}