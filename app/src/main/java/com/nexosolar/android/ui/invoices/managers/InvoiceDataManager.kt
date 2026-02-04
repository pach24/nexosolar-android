package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.RepositoryCallback
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gestor especializado en el ciclo de vida de los datos de facturas.
 */

class InvoiceDataManager(private val getInvoicesUseCase: GetInvoicesUseCase) {

    // ===== Variables de instancia =====

    private var _originalInvoices = mutableListOf<Invoice>()

    /**
     * Lista original completa (copia inmutable para seguridad).
     */
    val originalInvoices: List<Invoice> get() = _originalInvoices.toList()

    private val _invoices = MutableLiveData<List<Invoice>>()

    /**
     * LiveData observable para la UI.
     */
    val invoices: LiveData<List<Invoice>> get() = _invoices

    // ===== Métodos de operación con Corrutinas =====

    /**
     * Carga facturas de forma asíncrona.
     * @return La lista de facturas cargadas.
     * @throws Exception si ocurre un error en el caso de uso.
     */
    suspend fun loadInvoices(): List<Invoice> {
        // 1. Estrategia de caché: si ya tenemos datos, no vamos a red
        if (hasCachedData()) {
            return originalInvoices
        }

        // 2. Si no hay caché, suspendemos hasta obtener respuesta
        return suspendCancellableCoroutine { continuation ->
            getInvoicesUseCase.invoke(object : RepositoryCallback<List<Invoice>> {
                override fun onSuccess(result: List<Invoice>) {
                    updateOriginalInvoices(result)
                    _invoices.postValue(result)
                    continuation.resume(result)
                }

                override fun onError(error: Throwable) {
                    continuation.resumeWithException(error)
                }
            })
        }
    }

    /**
     * Fuerza la actualización de datos desde el servidor.
     */
    suspend fun refreshInvoices(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            getInvoicesUseCase.refresh(object : RepositoryCallback<Boolean> {
                override fun onSuccess(success: Boolean) {
                    // Al refrescar con éxito, limpiamos caché para forzar recarga real
                    _originalInvoices.clear()
                    continuation.resume(success)
                }

                override fun onError(error: Throwable) {
                    continuation.resumeWithException(error)
                }
            })
        }
    }

    // ===== Métodos de gestión de estado =====

    fun setInvoices(invoices: List<Invoice>?) {
        _invoices.postValue(invoices?.toList() ?: emptyList())
    }

    fun updateOriginalInvoices(invoices: List<Invoice>) {
        _originalInvoices = invoices.toMutableList()
    }

    // ===== Métodos de consulta (Los que te faltaban) =====

    /**
     * Verifica si hay datos en memoria.
     */
    fun hasCachedData(): Boolean = _originalInvoices.isNotEmpty()

    /**
     * Retorna la cantidad total de facturas originales.
     */
    fun getOriginalCount(): Int = _originalInvoices.size

    /**
     * Limpia el LiveData notificando una lista vacía.
     */
    fun invalidateCache() {
        _invoices.postValue(emptyList())
    }

    /**
     * Limpia completamente el estado del manager.
     */
    fun clearAllData() {
        _originalInvoices.clear()
        _invoices.postValue(emptyList())
    }
}