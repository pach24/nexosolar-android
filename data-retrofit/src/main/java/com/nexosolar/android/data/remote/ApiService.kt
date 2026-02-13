package com.nexosolar.android.data.remote

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockCircular
import co.infinum.retromock.meta.MockResponse
import retrofit2.http.GET

/**
 * Definición de servicios API con soporte para Retrofit y Retromock.
 *
 * Define endpoints y sus respuestas simuladas para testing sin backend.
 * Usa suspend functions para integración nativa con Coroutines.
 */
interface ApiService {

    /**
     * Obtiene la lista de facturas (suspendable).
     *
     * En modo Mock, utiliza [MockCircular] para rotar entre diferentes respuestas JSON:
     * 1. `facturas_500.json` (Error de servidor)
     * 2. `facturas_todas_impagadas.json` (Todas pendientes)
     * 3. `facturas_algunas_pagadas.json` (Mezcla pagadas/pendientes)
     * 4. `facturas_todas_pagadas.json` (Todas pagadas)
     *
     * En modo Real, llama al endpoint `invoices.json`.
     *
     * @return Respuesta con la lista de facturas
     * @throws retrofit2.HttpException Si el servidor devuelve error (4xx/5xx)
     * @throws java.io.IOException Si hay error de red
     */
    @Mock
    @MockCircular
    //@MockResponse(body = "facturas_invierno.json")
    //@MockResponse(body = "facturas_verano.json")
    @MockResponse(body = "facturas_500.json")
    @MockResponse(body = "facturas_todas_impagadas.json")
    @MockResponse(body = "facturas_algunas_pagadas.json")
    @MockResponse(body = "facturas_todas_pagadas.json")
    @GET("invoices.json")
    suspend fun getFacturas(): InvoiceResponse

    /**
     * Obtiene los detalles de la instalación solar (Smart Solar).
     *
     * En modo Mock, devuelve el contenido de `installation_details.json`.
     * En modo Real, llamaría al endpoint `installation_details.json`.
     *
     * @return Detalles de la instalación
     * @throws retrofit2.HttpException Si el servidor devuelve error (4xx/5xx)
     * @throws java.io.IOException Si hay error de red
     */
    @Mock
    @MockResponse(body = "installation_details.json")
    @GET("installation_details.json")
    suspend fun getInstallationDetails(): InstallationDTO
}
