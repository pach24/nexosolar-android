package com.nexosolar.android.domain.repository

import com.nexosolar.android.domain.models.Installation

/**
 * Contrato de repositorio para la gestión de datos de instalaciones.
 * Abstrae la fuente de datos (API/Cache) de la capa de dominio.
 *
 * Usa corrutinas para operaciones asíncronas. El manejo de errores se realiza
 * mediante excepciones que los casos de uso capturan con try-catch.
 */
interface InstallationRepository {

    /**
     * Recupera los detalles técnicos de la instalación asociada al usuario.
     *
     * @return Detalles de la instalación solar
     * @throws Exception si ocurre un error de red o parsing
     */
    suspend fun getInstallationDetails(): Installation
}
