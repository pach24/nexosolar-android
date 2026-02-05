package com.nexosolar.android.data.repository

import com.nexosolar.android.data.InstallationMapper
import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.data.remote.InstallationDTO
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Implementación del repositorio para gestionar datos de instalaciones solares.
 *
 * Actúa como intermediario entre la capa de dominio y la fuente de datos remota (API).
 * Solo maneja datos remotos ya que la información de instalaciones se consulta
 * bajo demanda y no requiere persistencia local.
 */
class InstallationRepositoryImpl(
    private val apiService: ApiService
) : InstallationRepository {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Obtiene los detalles de la instalación desde la API remota.
     *
     * @param callback Callback para notificar el resultado de la operación
     */
    override fun getInstallationDetails(callback: InstallationRepository.InstallationCallback) {
        apiService.getInstallationDetails().enqueue(object : Callback<InstallationDTO> {
            override fun onResponse(call: Call<InstallationDTO>, response: Response<InstallationDTO>) {
                executor.execute {
                    if (response.isSuccessful && response.body() != null) {
                        val installation = InstallationMapper.toDomain(response.body()!!)
                        callback.onSuccess(installation)
                    } else {
                        callback.onError("Error del servidor: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<InstallationDTO>, t: Throwable) {
                executor.execute {
                    callback.onError("Error de conexión: ${t.message}")
                }
            }
        })
    }
}
