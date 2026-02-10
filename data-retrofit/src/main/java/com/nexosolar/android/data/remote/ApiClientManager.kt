package com.nexosolar.android.data.remote

import android.content.Context
import co.infinum.retromock.BodyFactory
import co.infinum.retromock.Retromock
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDate

/**
 * Gestor centralizado de clientes Retrofit y Retromock.
 *
 * Proporciona instancias cacheadas de [ApiService] en tres modos:
 * - Mock (Retromock con datos locales)
 * - Real URL Principal
 * - Real URL Alternativa
 *
 * Implementa lazy initialization thread-safe usando delegados de Kotlin.
 */
object ApiClientManager {

    // Context requerido (no nullable)
    private lateinit var appContext: Context

    /**
     * Inicializa el manager con el contexto de aplicación.
     * Debe llamarse antes de usar [getApiService].
     *
     * @param context Contexto de la aplicación (se guardará el ApplicationContext)
     */
    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    // ========== GSON COMPARTIDO ==========

    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter)
            .create()
    }

    // ========== CLIENTES RETROFIT (Thread-safe) ==========

    private val retrofitClientUrl1: Retrofit by lazy {
        buildRetrofit(BuildConfig.API_BASE_URL)
    }

    private val retrofitClientUrl2: Retrofit by lazy {
        buildRetrofit(BuildConfig.API_BASE_URL_2)
    }

    private val retromockClient: Retromock by lazy {
        checkInitialized()
        Retromock.Builder()
            .retrofit(retrofitClientUrl1)
            .defaultBodyFactory(object : BodyFactory {
                @Throws(IOException::class)
                override fun create(input: String) =
                    appContext.assets.open(input)
            })
            .build()
    }

    // ========== SERVICIOS API (Thread-safe) ==========

    private val mockApiService: ApiService by lazy {
        retromockClient.create(ApiService::class.java)
    }

    private val realApiServiceUrl1: ApiService by lazy {
        retrofitClientUrl1.create(ApiService::class.java)
    }

    private val realApiServiceUrl2: ApiService by lazy {
        retrofitClientUrl2.create(ApiService::class.java)
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Obtiene la instancia correcta de [ApiService] según la configuración.
     *
     * @param useMock Si true, usa datos simulados (Retromock)
     * @param useAlternativeUrl Si true, usa URL alternativa (solo si useMock=false)
     * @return Instancia configurada de [ApiService]
     * @throws IllegalStateException Si no se llamó [init] antes
     */
    fun getApiService(
        useMock: Boolean,
        useAlternativeUrl: Boolean
    ): ApiService {
        return when {
            useMock -> mockApiService
            useAlternativeUrl -> realApiServiceUrl2
            else -> realApiServiceUrl1
        }
    }

    /**
     * Versión legacy que acepta Context (retrocompatibilidad).
     * El Context solo se usa si no se llamó [init] antes.
     */
    @Deprecated(
        message = "Use getApiService(useMock, useAlternativeUrl) after calling init()",
        replaceWith = ReplaceWith("getApiService(useMock, useAlternativeUrl)")
    )
    fun getApiService(
        useMock: Boolean,
        useAlternativeUrl: Boolean,
        context: Context
    ): ApiService {
        if (!::appContext.isInitialized) {
            init(context)
        }
        return getApiService(useMock, useAlternativeUrl)
    }

    // ========== HELPERS PRIVADOS ==========

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun checkInitialized() {
        check(::appContext.isInitialized) {
            "ApiClientManager not initialized. Call init(context) first."
        }
    }
}
