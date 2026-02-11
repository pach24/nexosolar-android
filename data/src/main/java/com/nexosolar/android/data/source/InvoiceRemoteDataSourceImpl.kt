package com.nexosolar.android.data.source

import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.remote.ApiService

/**
 * Implementación del origen de datos remoto para facturas usando Retrofit con corrutinas.
 *
 * Encapsula toda la lógica de comunicación con la API REST, delegando
 * el manejo de hilos y errores a Retrofit mediante suspend functions.
 */
class InvoiceRemoteDataSourceImpl(
    private val apiService: ApiService
) : InvoiceRemoteDataSource {

    private val mapper = InvoiceMapper

    /**
     * Obtiene las facturas desde la API y las convierte a entidades de Room.
     *
     * @return Lista de entidades listas para persistir en base de datos
     * @throws Exception Si hay error de red o del servidor
     */
    override suspend fun getFacturas(): List<InvoiceEntity> {
        val response = apiService.getFacturas()  // ⚡ Llamada directa
        return mapper.toEntityListFromDto(response.facturas?.filterNotNull())
    }


}
