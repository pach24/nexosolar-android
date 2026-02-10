package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InstallationMapper
import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación del repositorio para gestionar datos de instalaciones solares con corrutinas.
 *
 * Actúa como intermediario entre la capa de dominio y la fuente de datos remota (API).
 * Solo maneja datos remotos ya que la información de instalaciones se consulta
 * bajo demanda y no requiere persistencia local.
 */
class InstallationRepositoryImpl(
    private val apiService: ApiService
) : InstallationRepository {

    override suspend fun getInstallationDetails(): Installation {
        return withContext(Dispatchers.IO) {
            val dto = apiService.getInstallationDetails()
            InstallationMapper.toDomain(dto) ?: throw Exception("Error al mapear instalación")
        }
    }
}
