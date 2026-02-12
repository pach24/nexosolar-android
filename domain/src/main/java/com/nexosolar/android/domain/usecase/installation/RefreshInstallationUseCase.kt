package com.nexosolar.android.domain.usecase.installation

import com.nexosolar.android.domain.repository.InstallationRepository

class RefreshInstallationUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke() {
        repository.refreshInstallation()
    }
}
