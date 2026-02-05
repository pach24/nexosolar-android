package com.nexosolar.android.data.source

import com.nexosolar.android.data.local.InvoiceEntity

/**
 * Interfaz para el origen de datos remoto de facturas.
 *
 * Abstrae la capa de red usando corrutinas para operaciones asíncronas.
 */
interface InvoiceRemoteDataSource {

    /**
     * Obtiene la lista de facturas desde la fuente remota.
     *
     * @return Lista de entidades de facturas
     * @throws Exception si ocurre un error de red o parsing
     */
    suspend fun getFacturas(): List<InvoiceEntity>
}
