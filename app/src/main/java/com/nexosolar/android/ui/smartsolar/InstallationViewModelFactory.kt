package com.nexosolar.android.ui.smartsolar

import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nexosolar.android.domain.repository.InstallationRepository
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase
import com.nexosolar.android.domain.usecase.installation.RefreshInstallationUseCase

/**
 * Factory para la creación de InstallationViewModel con inyección manual de dependencias.
 *
 * Responsabilidades:
 * - Construir la cadena de dependencias (Repository -> UseCase -> ViewModel)
 * - Respetar la configuración global del DataModule (Mock/Real)
 */
class InstallationViewModelFactory(
    private val repository: InstallationRepository
) : ViewModelProvider.Factory {

    @NonNull
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstallationViewModel::class.java)) {
            val getUseCase = GetInstallationDetailsUseCase(repository)
            val refreshUseCase = RefreshInstallationUseCase(repository)
            return InstallationViewModel(getUseCase, refreshUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
