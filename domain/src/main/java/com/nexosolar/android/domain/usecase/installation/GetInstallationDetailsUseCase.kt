package com.nexosolar.android.domain.usecase.installation

import com.nexosolar.android.domain.repository.InstallationRepository
import com.nexosolar.android.domain.repository.InstallationRepository.InstallationCallback

/**
 * Caso de uso para recuperar los detalles técnicos de la instalación asociada al usuario.
 * Coordina la llamada al repositorio y proporciona un punto centralizado para extender
 * lógica de negocio (validaciones, transformaciones, auditoría) sin afectar la capa de presentación.
 */
class GetInstallationDetailsUseCase( private val repository: InstallationRepository){

    /**
     * Ejecuta la obtención de detalles de instalación.
     * Actualmente delega directamente al repositorio, pero permite extender
     * con validaciones previas o transformaciones posteriores.
     */
    operator fun invoke(callback: InstallationCallback?) {

        repository.getInstallationDetails(callback)
    }
}
