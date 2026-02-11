// ui/invoices/InvoiceViewModel.kt
package com.nexosolar.android.ui.invoices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.domain.models.newestDate
import com.nexosolar.android.domain.models.oldestDate
import com.nexosolar.android.domain.models.maxAmount
import com.nexosolar.android.domain.models.minAmount


import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.ui.invoices.managers.InvoiceStateManager
import com.nexosolar.android.ui.invoices.InvoiceFilterUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para la pantalla de listado de facturas.
 *
 * Responsabilidades:
 * - Orquestar la carga de facturas mediante UseCases
 * - Gestionar el estado de UI (loading, error, empty, data)
 * - Coordinar el filtrado de facturas localmente
 * - Exponer LiveData para observación desde la UI
 *
 * Nota: NO contiene lógica de negocio. Toda la lógica está en UseCases y el dominio.
 */
class InvoiceViewModel(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase
) : ViewModel() {

    // ========== COMPANION ==========

    private companion object {
        private const val TAG = "InvoiceViewModel"
    }

    // ========== ESTADO PRIVADO ==========

    /**
     * Copia local de facturas originales para filtrado rápido.
     */
    private var originalInvoices: List<Invoice> = emptyList()

    private val stateManager = InvoiceStateManager()

    // ========== LIVEDATA PRIVADO ==========

    private val _facturas = MutableLiveData<List<Invoice>>()
    private val _filterUIState = MutableLiveData(InvoiceFilterUIState())

    // ========== LIVEDATA PÚBLICO ==========

    /**
     * Lista de facturas actual (puede estar filtrada o completa).
     */
    val facturas: LiveData<List<Invoice>> = _facturas

    /**
     * Estado completo de los filtros y sus estadísticas.
     * Incluye: filtros actuales, importes máx/mín, rango de fechas.
     */
    val filterUIState: LiveData<InvoiceFilterUIState> = _filterUIState

    /**
     * Estado visual de la pantalla (loading, error, data, empty).
     */
    val viewState: LiveData<InvoiceStateManager.ViewState> = stateManager.currentState

    /**
     * Mensaje de error descriptivo (si aplica).
     */
    val errorMessage: LiveData<String?> = stateManager.errorMessage

    /**
     * Indica si debe mostrarse la pantalla de error vacía.
     */
    val showEmptyError: LiveData<Boolean> = stateManager.showEmptyError

    // ========== INIT ==========

    init {
        cargarFacturas()
    }

    // ========== MÉTODOS PÚBLICOS - CARGA DE DATOS ==========

    /**
     * Carga facturas desde el repositorio.
     * Delega al UseCase la obtención de datos.
     */
    fun cargarFacturas() {
        Logger.d(TAG, "[START] Loading invoices...")
        stateManager.showLoading()

        viewModelScope.launch {
            try {
                val result = getInvoicesUseCase()
                Logger.d(TAG, "[SUCCESS] Received ${result.size} invoices")

                originalInvoices = result

                if (result.isEmpty()) {
                    Logger.d(TAG, "[EMPTY] No invoices found")
                    stateManager.showEmpty()
                } else {
                    initializeFilterState(result)
                    _facturas.postValue(result)
                    stateManager.showData()
                }
            } catch (e: Exception) {
                Logger.e(TAG, "[ERROR] Failed to load: ${e.message}", e)
                handleLoadError(e)
            }
        }
    }

    // ========== MÉTODOS PÚBLICOS - GESTIÓN DE FILTROS ==========

    /**
     * Actualiza los filtros sin aplicarlos inmediatamente.
     * Normaliza automáticamente los filtros según reglas de negocio.
     * Usado para cambios en tiempo real desde la UI (slider, checkboxes, etc.).
     *
     * @param filters Nuevos filtros a actualizar
     */
    fun updateFilters(filters: InvoiceFilters) {
        // ✅ Normalizamos automáticamente (intercambia fechas/importes si es necesario)
        val normalizedFilters = filters.normalize()

        _filterUIState.value = _filterUIState.value?.copy(filters = normalizedFilters)

        Logger.d(TAG, "[FILTER] Updated: dates=${normalizedFilters.startDate} to ${normalizedFilters.endDate}, amounts=${normalizedFilters.minAmount} to ${normalizedFilters.maxAmount}")
    }

    /**
     * Aplica los filtros actuales a la lista de facturas.
     * Llamado cuando el usuario presiona "Aplicar" en el FilterFragment.
     */
    fun applyFilters() {
        Logger.d(TAG, "[FILTER] Applying filters...")

        // Indicamos que estamos aplicando filtros
        _filterUIState.value = _filterUIState.value?.copy(isApplying = true)

        stateManager.showLoading()

        viewModelScope.launch {
            delay(200) // UX: Pequeño delay para transición visual
            aplicarFiltrosActuales()

            // Finalizamos el estado de aplicación
            _filterUIState.value = _filterUIState.value?.copy(isApplying = false)
        }
    }

    /**
     * Resetea todos los filtros y muestra la lista completa.
     */
    fun resetearFiltros() {
        Logger.d(TAG, "[FILTER] Resetting all filters")

        if (originalInvoices.isNotEmpty()) {
            initializeFilterState(originalInvoices)
            _facturas.value = originalInvoices
            stateManager.showData()
        }
    }

    // ========== MÉTODOS PÚBLICOS - CONSULTAS DE ESTADO ==========

    /**
     * Verifica si hay datos cargados.
     * Usado para habilitar/deshabilitar el botón de filtros.
     */
    fun hayDatosCargados(): Boolean {
        return originalInvoices.isNotEmpty()
    }

    /**
     * Verifica si el estado actual es de error.
     */
    fun esEstadoError(): Boolean {
        return stateManager.isError()
    }

    // ========== MÉTODOS PRIVADOS - LÓGICA INTERNA ==========

    /**
     * Inicializa el estado de filtros basándose en las estadísticas de las facturas.
     * Usa extension functions para calcular estadísticas.
     *
     * @param invoices Lista de facturas para calcular estadísticas
     */
    private fun initializeFilterState(invoices: List<Invoice>) {
        // ✅ Usamos extension functions en lugar de Calculator
        val maxAmount = invoices.maxAmount()
        val oldestDate = invoices.oldestDate()
        val newestDate = invoices.newestDate()

        Logger.d(TAG, "[STATS] Max: $maxAmount, Range: $oldestDate to $newestDate")

        // Creamos el estado inicial de filtros
        _filterUIState.value = InvoiceFilterUIState(
            filters = InvoiceFilters(
                minAmount = 0f,
                maxAmount = maxAmount,
                startDate = null,
                endDate = null,
                filteredStates = emptySet()
            ),
            statistics = InvoiceFilterUIState.FilterStatistics(
                maxAmount = maxAmount,
                oldestDateMillis = DateUtils.toEpochMilli(oldestDate),
                newestDateMillis = DateUtils.toEpochMilli(newestDate)
            ),
            isApplying = false
        )
    }

    /**
     * Aplica los filtros actuales a la lista de facturas.
     * Delega la lógica de filtrado al UseCase de dominio.
     */
    private suspend fun aplicarFiltrosActuales() {
        if (originalInvoices.isEmpty()) {
            Logger.d(TAG, "[FILTER] No data to filter")
            return
        }

        val filteredList = withContext(Dispatchers.Default) {
            val currentFilters = _filterUIState.value?.filters

            if (currentFilters == null) {
                Logger.d(TAG, "[FILTER] No filters applied, returning all data")
                originalInvoices
            } else {
                val result = filterInvoicesUseCase(
                    invoices = originalInvoices,
                    filters = currentFilters
                )
                Logger.d(TAG, "[FILTER] Applied: ${result.size}/${originalInvoices.size} invoices match")
                result
            }
        }

        _facturas.value = filteredList
        stateManager.showData()
    }

    /**
     * Maneja errores de carga clasificándolos y actualizando el estado.
     *
     * @param error Excepción capturada durante la carga
     */
    private fun handleLoadError(error: Throwable) {
        val errorType = ErrorClassifier.classify(error)
        Logger.e(TAG, "[ERROR] Classified as: ${errorType.javaClass.simpleName}")
        stateManager.showError(errorType)
    }
}
