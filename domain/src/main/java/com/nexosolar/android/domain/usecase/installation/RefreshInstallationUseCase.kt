package com.nexosolar.android.domain.usecase.installation

import com.nexosolar.android.domain.repository.InstallationRepository
import javax.inject.Inject

class RefreshInstallationUseCase @Inject constructor(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke() {
        repository.refreshInstallation()
    }
}
