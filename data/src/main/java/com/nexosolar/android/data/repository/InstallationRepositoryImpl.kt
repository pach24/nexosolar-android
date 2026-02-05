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

    override suspend fun getInstallationDetails(): Installation = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val call = apiService.getInstallationDetails()

            call.enqueue(object : retrofit2.Callback<com.nexosolar.android.data.remote.InstallationDTO> {
                override fun onResponse(
                    call: retrofit2.Call<com.nexosolar.android.data.remote.InstallationDTO>,
                    response: retrofit2.Response<com.nexosolar.android.data.remote.InstallationDTO>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val installation = InstallationMapper.toDomain(response.body()!!)
                        if (installation != null) {
                            continuation.resume(installation)
                        } else {
                            continuation.resumeWithException(
                                Exception("Error al mapear instalación")
                            )
                        }
                    } else {
                        continuation.resumeWithException(
                            Exception("Error del servidor: ${response.code()}")
                        )
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.nexosolar.android.data.remote.InstallationDTO>,
                    t: Throwable
                ) {
                    continuation.resumeWithException(t)
                }
            })

            // Cancela la llamada si la coroutine se cancela
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }
}
