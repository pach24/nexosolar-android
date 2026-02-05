package com.nexosolar.android.data.source

import com.nexosolar.android.data.InvoiceMapper
import com.nexosolar.android.data.local.InvoiceEntity
import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.data.remote.InvoiceResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación del origen de datos remoto para facturas usando Retrofit con corrutinas.
 *
 * Encapsula toda la lógica de comunicación con la API REST, convirtiendo
 * los callbacks de Retrofit en suspend functions mediante suspendCancellableCoroutine.
 */
class InvoiceRemoteDataSourceImpl(
    private val apiService: ApiService
) : InvoiceRemoteDataSource {

    private val mapper = InvoiceMapper

    override suspend fun getFacturas(): List<InvoiceEntity> {
        return suspendCancellableCoroutine { continuation ->
            val call = apiService.getFacturas()

            call.enqueue(object : retrofit2.Callback<InvoiceResponse> {
                override fun onResponse(
                    call: retrofit2.Call<InvoiceResponse>,
                    response: retrofit2.Response<InvoiceResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val entities = mapper.toEntityListFromDto(response.body()?.facturas)
                        continuation.resume(entities)
                    } else {
                        continuation.resumeWithException(
                            Exception("Error del servidor: ${response.code()}")
                        )
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<InvoiceResponse>,
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
