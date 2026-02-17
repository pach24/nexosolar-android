package com.nexosolar.android.ui.smartsolar.models

import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Installation

sealed interface InstallationUIState {
    // Carga inicial (Shimmer a pantalla completa)
    data object Loading : InstallationUIState

    // Sin datos (Pantalla vacía, pero permite pull-to-refresh)
    data class Empty(val isRefreshing: Boolean = false) : InstallationUIState

    // Éxito (Muestra datos + estado del spinner de recarga)
    data class Success(
        val installation: Installation,
        val isRefreshing: Boolean = false
    ) : InstallationUIState

    // Error (Pantalla de error)
    data class Error(
        val messageRes: Int,
        val type: ErrorClassifier.ErrorType
    ) : InstallationUIState
}