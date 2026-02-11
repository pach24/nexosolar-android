package com.nexosolar.android.ui.invoices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.ui.invoices.managers.InvoiceFilterManager
import com.nexosolar.android.ui.invoices.managers.InvoiceStateManager
import com.nexosolar.android.ui.invoices.managers.InvoiceStatisticsCalculator
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
 * Nota: NO contiene lógica de negocio. Toda la lógica está en UseCases y Repository.
 */
class InvoiceViewModel(
    private val getInvoicesUseCase: GetInvoicesUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "InvoiceViewModel"
    }

    // --- ESTADO DE DATOS ---

    private val _facturas = MutableLiveData<List<Invoice>>()
    val facturas: LiveData<List<Invoice>> = _facturas

    /**
     * Copia local de facturas originales para filtrado rápido en UI.
     */
    private var originalInvoices: List<Invoice> = emptyList()

    // --- MANAGERS ---

    private val filterManager = InvoiceFilterManager()
    private val stateManager = InvoiceStateManager()
    private val statisticsCalculator = InvoiceStatisticsCalculator()

    // --- LIVEDATA EXPUESTO ---

    val viewState = stateManager.currentState
    val filtrosActuales = filterManager.currentFilters
    val showEmptyError = stateManager.showEmptyError
    val errorMessage = stateManager.errorMessage

    init {
        cargarFacturas()
    }

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
                    filterManager.resetFilters(result)
                    _facturas.postValue(result)
                    stateManager.showData()
                }
            } catch (e: Exception) {
                Logger.e(TAG, "[ERROR] Failed to load: ${e.message}", e)
                handleLoadError(e)
            }
        }
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
            val currentFilters = filterManager.getCurrentFiltersSnapshot()

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
     * Actualiza los filtros y aplica el filtrado.
     * Llamado desde la UI cuando el usuario aplica nuevos filtros.
     *
     * @param filters Nuevos filtros a aplicar
     */
    fun actualizarFiltros(filters: InvoiceFilters) {
        Logger.d(TAG, "[FILTER] Updating filters...")
        filterManager.updateFilters(filters)
        stateManager.showLoading()

        viewModelScope.launch {
            delay(200) // UX: Pequeño delay para transición visual
            aplicarFiltrosActuales()
        }
    }

    /**
     * Resetea todos los filtros y muestra la lista completa.
     */
    fun resetearFiltros() {
        Logger.d(TAG, "[FILTER] Resetting all filters")
        filterManager.resetFilters(originalInvoices)
        _facturas.value = originalInvoices
        stateManager.showData()
    }

    // --- MÉTODOS AUXILIARES ---

    /**
     * Actualiza el estado de filtros sin aplicarlos inmediatamente.
     * Usado para actualizar filtros en tiempo real desde la UI.
     */
    fun actualizarEstadoFiltros(filters: InvoiceFilters) {
        filterManager.updateFilters(filters)
    }

    /**
     * Obtiene el importe máximo de todas las facturas.
     * Usado para configurar el slider de rango de importes.
     */
    fun getMaxImporte(): Float {
        return statisticsCalculator.calculateMaxAmount(originalInvoices)
    }

    /**
     * Obtiene la fecha más antigua en milisegundos (para DatePicker).
     */
    fun getOldestDateMillis(): Long {
        return DateUtils.toEpochMilli(statisticsCalculator.calculateOldestDate(originalInvoices))
    }

    /**
     * Obtiene la fecha más reciente en milisegundos (para DatePicker).
     */
    fun getNewestDateMillis(): Long {
        return DateUtils.toEpochMilli(statisticsCalculator.calculateNewestDate(originalInvoices))
    }

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

    /**
     * Maneja errores de carga clasificándolos y actualizando el estado.
     */
    private fun handleLoadError(error: Throwable) {
        val errorType = ErrorClassifier.classify(error)
        Logger.e(TAG, "[ERROR] Classified as: ${errorType.javaClass.simpleName}")
        stateManager.showError(errorType)
    }
}
