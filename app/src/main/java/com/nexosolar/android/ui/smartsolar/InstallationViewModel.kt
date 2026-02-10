package com.nexosolar.android.ui.smartsolar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.toUserMessage
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase
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
class InstallationViewModel(
    getInstallationDetailsUseCase: GetInstallationDetailsUseCase
) : ViewModel() {

    // ===== Managers =====

    private val dataManager = InstallationDataManager(getInstallationDetailsUseCase)
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
                dataManager.loadInstallationDetails()
                isFirstLoad = false
                stateManager.showData()

            } catch (e: Exception) {
                // Cualquier error que lance el use case caerá aquí automáticamente
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
        // 1️⃣ Early return si hay caché (igual que antes)
        if (dataManager.hasCachedData()) {
            stateManager.showData()
            return
        }

        // 2️⃣ Clasificas el error dentro del when (smart casting)
        //    ✅ El compilador sabe el tipo exacto en cada rama
        when (val errorType = ErrorClassifier.classify(error)) {

            // 3️⃣ Caso Network: errorType es automáticamente ErrorType.Network
            //    ✅ Puedes acceder a errorType.details si lo necesitas
            is ErrorClassifier.ErrorType.Network -> {
                viewModelScope.launch {
                    delay(3000)
                    // ✅ Llamada fluida: errorType.toUserMessage()
                    stateManager.showNetworkError(errorType.toUserMessage())
                }
            }

            // 4️⃣ Caso Server: errorType es automáticamente ErrorType.Server
            is ErrorClassifier.ErrorType.Server -> {
                stateManager.showServerError(errorType.toUserMessage())
            }

            // 5️⃣ Caso Unknown: errorType es automáticamente ErrorType.Unknown
            is ErrorClassifier.ErrorType.Unknown -> {
                stateManager.showServerError(errorType.toUserMessage())
            }

            // ⚠️ NO necesitas `else` - el compilador verifica exhaustividad
        }
    }

}
