package com.nexosolar.android.ui.smartsolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.Logger
import com.nexosolar.android.core.toTechnicalMessage
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase
import com.nexosolar.android.domain.usecase.installation.RefreshInstallationUseCase
import com.nexosolar.android.ui.common.toUserMessageRes
import com.nexosolar.android.ui.smartsolar.models.InstallationUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstallationViewModel @Inject constructor(
    private val getInstallationUseCase: GetInstallationDetailsUseCase,
    private val refreshInstallationUseCase: RefreshInstallationUseCase
) : ViewModel() {

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
                    // Evitamos parpadeo de carga si ya hay datos mostrándose
                    if (_uiState.value !is InstallationUIState.Success) {
                        _uiState.emit(InstallationUIState.Loading)
                    }
                }
                .catch { error ->
                    val errorType = ErrorClassifier.classify(error)
                    Logger.e("InstallationVM", errorType.toTechnicalMessage(), error)
                    _uiState.emit(InstallationUIState.Error(errorType.toUserMessageRes(), errorType))
                }
                .collect { installation ->
                    // La emisión exitosa de Room detiene el spinner automáticamente
                    _uiState.value = if (installation == null) {
                        InstallationUIState.Empty(isRefreshing = false)
                    } else {
                        InstallationUIState.Success(installation, isRefreshing = false)
                    }
                }
        }
    }

    fun onRefresh() {
        val currentState = _uiState.value
        // Si hay error previo, reintentamos la observación completa
        if (currentState is InstallationUIState.Error) {
            observarInstalacion()
            return
        }
        // Refresco silencioso para Success/Empty
        viewModelScope.launch {
            setRefreshing(true)
            try {
                refreshInstallationUseCase()
            } catch (e: Exception) {
                Logger.e("InstallationVM", "Error al refrescar", e)
                setRefreshing(false)
            }
        }
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        _uiState.update { state ->
            when (state) {
                is InstallationUIState.Success -> state.copy(isRefreshing = isRefreshing)
                is InstallationUIState.Empty -> state.copy(isRefreshing = isRefreshing)
                else -> state
            }
        }
    }
}
