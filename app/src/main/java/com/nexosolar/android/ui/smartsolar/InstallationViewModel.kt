package com.nexosolar.android.ui.smartsolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.toTechnicalMessage
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstallationViewModel(
    private val getInstallationUseCase: GetInstallationDetailsUseCase,
    private val refreshInstallationUseCase: RefreshInstallationUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "InstallationVM"
    }

    private val _uiState = MutableStateFlow<InstallationUIState>(InstallationUIState.Loading)
    val uiState: StateFlow<InstallationUIState> = _uiState.asStateFlow()

    private var collectionJob: Job? = null

    init {
        observarInstalacion()
    }

    private fun observarInstalacion() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            getInstallationUseCase()
                .onStart {
                    // Solo ponemos Loading si es la primera vez (no hay datos previos)
                    if (_uiState.value !is InstallationUIState.Success) {
                        _uiState.value = InstallationUIState.Loading
                    }
                }
                .catch { error ->
                    val errorType = ErrorClassifier.classify(error)
                    Logger.e(TAG, errorType.toTechnicalMessage(), error)
                    _uiState.value = InstallationUIState.Error(
                        message = errorType.toUserMessage(),
                        type = errorType
                    )
                }
                .collect { installation ->
                    Logger.d(TAG, "[FLOW] Received installation data")
                    // Al recibir datos nuevos de Room, el refresh termina automáticamente (isRefreshing = false)
                    if (installation == null) {
                        _uiState.value = InstallationUIState.Empty(isRefreshing = false)
                    } else {
                        _uiState.value = InstallationUIState.Success(installation, isRefreshing = false)
                    }
                }
        }
    }

    /**
     *
     * Mantiene los datos visibles y solo activa el spinner.
     */
    fun onRefresh() {
        val currentState = _uiState.value

        // Solo permitimos refresh si estamos en Success o Empty
        if (currentState is InstallationUIState.Success || currentState is InstallationUIState.Empty) {
            viewModelScope.launch {
                // 1. Activar spinner visualmente (sin borrar datos)
                if (currentState is InstallationUIState.Success) {
                    _uiState.update { currentState.copy(isRefreshing = true) }
                } else if (currentState is InstallationUIState.Empty) {
                    _uiState.update { currentState.copy(isRefreshing = true) }
                }

                // 2. Llamada de red
                try {
                    refreshInstallationUseCase()
                    // Si va bien, Room se actualiza -> 'observarInstalacion' emite nuevo valor -> Spinner se apaga solo
                } catch (error: Exception) {
                    Logger.e(TAG, "Error refrescando instalación", error)

                    // 3. Si falla, apagamos el spinner manualmente y mantenemos los datos viejos
                    // (Opcional: aquí podrías emitir un evento OneShot para un Toast de error)
                    if (currentState is InstallationUIState.Success) {
                        _uiState.update { currentState.copy(isRefreshing = false) }
                    } else if (currentState is InstallationUIState.Empty) {
                        _uiState.update { currentState.copy(isRefreshing = false) }
                    }

                    // NOTA: Si quisieras mostrar Toast, necesitarías un Channel de efectos (SideEffects),
                    // pero para mantenerlo simple como Invoices, solo paramos el spinner.
                }
            }
        } else {
            // Si estamos en Error y damos a "Reintentar", hacemos carga completa
            observarInstalacion()
        }
    }
}
