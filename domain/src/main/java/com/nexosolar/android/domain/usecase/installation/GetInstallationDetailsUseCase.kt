package com.nexosolar.android.domain.usecase.installation

import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para recuperar los detalles técnicos de la instalación asociada al usuario.
 *
 * Coordina la llamada al repositorio y proporciona un punto centralizado para extender
 * lógica de negocio (validaciones, transformaciones, auditoría) sin afectar la capa de presentación.
 */
class GetInstallationDetailsUseCase(
    private val repository: InstallationRepository
) {

    /**
     * Ejecuta la obtención de detalles de instalación.
     *
     * Actualmente delega directamente al repositorio, pero permite extender
     * con validaciones previas o transformaciones posteriores.
     *
     * @return Detalles de la instalación solar
     * @throws Exception si ocurre un error de red o parsing
     */
    operator fun invoke(): Flow<Installation?> {
        // Aquí podrías añadir lógica de negocio antes/después de llamar al repo
        // Por ejemplo: validaciones, logging, caché, etc.
        return repository.getInstallationDetails()
    }
}
