package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase

/**
 * Gestor especializado en el ciclo de vida de los datos de facturas.
 *
 * Responsabilidades:
 * - Gestionar caché de facturas en memoria
 * - Coordinar carga y refresco de datos
 * - Exponer LiveData para observación desde la UI
 */
class InvoiceDataManager(
    private val getInvoicesUseCase: GetInvoicesUseCase
) {

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
     *
     * Estrategia: Si hay caché, lo retorna. Si no, consulta el repositorio.
     *
     * @return La lista de facturas cargadas
     * @throws Exception si ocurre un error en el caso de uso
     */
    suspend fun loadInvoices(): List<Invoice> {
        // 1. Estrategia de caché: si ya tenemos datos, no vamos a red
        if (hasCachedData()) {
            return originalInvoices
        }

        // 2. Si no hay caché, llamamos al use case
        val result = getInvoicesUseCase()
        updateOriginalInvoices(result)
        _invoices.postValue(result)
        return result
    }

    /**
     * Fuerza la actualización de datos desde el servidor.
     *
     * Limpia el caché local para forzar recarga en la siguiente consulta.
     *
     * @throws Exception si ocurre un error de red
     */
    suspend fun refreshInvoices() {
        getInvoicesUseCase.refresh()
        // Al refrescar con éxito, limpiamos caché para forzar recarga real
        _originalInvoices.clear()
    }

    // ===== Métodos de gestión de estado =====

    /**
     * Establece la lista de facturas en el LiveData.
     *
     * @param invoices Lista de facturas a mostrar, o null para lista vacía
     */
    fun setInvoices(invoices: List<Invoice>?) {
        _invoices.postValue(invoices?.toList() ?: emptyList())
    }

    /**
     * Actualiza la lista de facturas originales (caché interno).
     *
     * @param invoices Nueva lista de facturas
     */
    fun updateOriginalInvoices(invoices: List<Invoice>) {
        _originalInvoices = invoices.toMutableList()
    }

    // ===== Métodos de consulta =====

    /**
     * Verifica si hay datos en memoria.
     *
     * @return true si hay facturas cacheadas, false en caso contrario
     */
    fun hasCachedData(): Boolean = _originalInvoices.isNotEmpty()

    /**
     * Retorna la cantidad total de facturas originales.
     *
     * @return Número de facturas en caché
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
