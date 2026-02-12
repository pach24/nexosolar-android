// File: ui/smartsolar/InstallationViewModel.kt
package com.nexosolar.android.ui.smartsolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.toUserMessage
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase
import com.nexosolar.android.domain.usecase.installation.RefreshInstallationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class InstallationViewModel(
    private val getInstallationUseCase: GetInstallationDetailsUseCase,
    private val refreshInstallationUseCase: RefreshInstallationUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "InstallationVM"
    }

    // ========== STATEFLOW (Fuente de verdad) ==========
    private val _uiState = MutableStateFlow<InstallationUIState>(InstallationUIState.Loading)
    val uiState: StateFlow<InstallationUIState> = _uiState.asStateFlow()

    private var collectionJob: Job? = null

    init {
        observarInstalacion()
    }

    /**
     * Sigue el mismo patrón que InvoiceViewModel.observarFacturas()
     */
    private fun observarInstalacion() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {

            getInstallationUseCase()
                .onStart {
                    _uiState.value = InstallationUIState.Loading
                }
                .catch { error ->
                    Logger.e(TAG, "[ERROR] Flow error: ${error.message}", error)
                    val errorType = ErrorClassifier.classify(error)
                    _uiState.value = InstallationUIState.Error(
                        message = errorType.toUserMessage(),
                        type = errorType
                    )
                }
                .collect { installation ->
                    Logger.d(TAG, "[FLOW] Received installation data")
                    if (installation == null) {
                        _uiState.value = InstallationUIState.Empty
                    } else {
                        _uiState.value = InstallationUIState.Success(installation)
                    }
                }
        }
    }

    /**
     * Acción explícita de recarga (Swipe to Refresh).
     * Mantenemos la lógica separada del refresh para no "ensuciar" el observer.
     */
    fun onRefresh() {
        viewModelScope.launch {
            // Opcional: Si quieres mostrar loading completo como en Invoice:
            // observarInstalacion()

            // O MEJOR (UX Superior): Mantener los datos viejos mientras se actualiza
            // y solo mostrar error si falla, sin borrar la pantalla.
            try {
                refreshInstallationUseCase()
                // Al terminar, el Flow de observarInstalacion emitirá los nuevos datos automáticamente
            } catch (error: Exception) {
                Logger.e(TAG, "Error refrescando instalación", error)
                // Aquí podrías emitir un evento One-Shot (Toast) si quisieras
                // Pero por consistencia estricta, si falla la red, el Flow original
                // ya maneja errores de red si vienen del repositorio.
            }
        }
    }
}
