package com.nexosolar.android.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.nexosolar.android.core.Logger
import com.nexosolar.android.data.remote.InstallationDTO
import com.nexosolar.android.data.remote.InvoiceResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicApiService @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiService {

    init {
        ApiClientManager.init(context) // Inicializar una sola vez al crear el Proxy
    }

    // Necesitamos leer las preferencias para saber qué modo usar
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("RepoPrefs", Context.MODE_PRIVATE)
    }

    // Helper para obtener el servicio real en cada llamada
    private val currentService: ApiService
        get() {
            // Leemos la configuración guardada por DataModule/App
            val useMock = prefs.getBoolean("last_mode_was_mock", true)
            val useAltUrl = prefs.getBoolean("last_url_was_alt", false)


            Logger.d("DynamicApi", "Using service: Mock=$useMock, AltUrl=$useAltUrl")
            return ApiClientManager.getApiService(useMock, useAltUrl)
        }

    // Delegamos todas las llamadas
    override suspend fun getFacturas(): InvoiceResponse {
        return currentService.getFacturas()
    }

    override suspend fun getInstallationDetails(): InstallationDTO {
        return currentService.getInstallationDetails()
    }
}
