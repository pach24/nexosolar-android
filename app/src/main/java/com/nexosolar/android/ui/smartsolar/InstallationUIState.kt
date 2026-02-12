// File: ui/smartsolar/InstallationUIState.kt
package com.nexosolar.android.ui.smartsolar

import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Installation

sealed interface InstallationUIState {
    data object Loading : InstallationUIState
    data object Empty : InstallationUIState
    data class Success(val installation: Installation) : InstallationUIState
    data class Error(
        val message: String,
        val type: ErrorClassifier.ErrorType
    ) : InstallationUIState
}


