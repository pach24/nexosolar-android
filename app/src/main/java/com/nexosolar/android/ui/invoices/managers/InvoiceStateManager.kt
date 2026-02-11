package com.nexosolar.android.ui.invoices.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nexosolar.android.core.ErrorClassifier

/**
 * Gestor especializado en estados de UI para la pantalla de facturas.
 *
 * Responsabilidades:
 * - Gestionar el ciclo de vida de los estados visuales (carga, error, datos, vacío)
 * - Mantener consistencia entre múltiples indicadores de estado
 * - Proporcionar métodos seguros para transiciones de estado
 *
 * Implementa el patrón State para encapsular la lógica de transiciones de UI.
 */
class InvoiceStateManager {

    /**
     * Estados posibles de la UI de facturas.
     * Sigue una jerarquía clara: LOADING → (ERROR | DATA | EMPTY)
     */
    enum class ViewState {
        LOADING,        // Mostrando shimmer/indicador de carga
        ERROR_NETWORK,  // Error de conexión (wifi/red)
        ERROR_SERVER,   // Error del servidor (HTTP 4xx/5xx)
        EMPTY,          // Datos cargados pero lista vacía
        DATA            // Datos cargados y listos para mostrar
    }

    // ===== Variables de instancia =====
    private val _currentState = MutableLiveData(ViewState.LOADING)
    private val _errorMessage = MutableLiveData<String?>()
    private val _showEmptyError = MutableLiveData(false)

    // ===== Getters de LiveData =====

    /**
     * Obtiene el estado actual de la UI como LiveData.
     * @return LiveData con el estado actual (LOADING, ERROR_NETWORK, etc.)
     */
    val currentState: LiveData<ViewState> get() = _currentState

    /**
     * Obtiene el mensaje de error actual (si aplica).
     * @return LiveData con mensaje de error o null si no hay error
     */
    val errorMessage: LiveData<String?> get() = _errorMessage

    /**
     * Indica si se debe mostrar la pantalla de error vacía.
     * Diferencia entre "sin datos" (EMPTY) y "error que requiere acción" (showEmptyError=true).
     * @return LiveData booleano indicando visibilidad de error
     */
    val showEmptyError: LiveData<Boolean> get() = _showEmptyError

    // ===== Métodos de transición de estado =====

    /**
     * Activa el estado de carga (shimmer/loader visible).
     * Resetea cualquier error previo.
     */
    fun showLoading() {
        _currentState.value = ViewState.LOADING
        _errorMessage.value = null
        _showEmptyError.value = false
    }

    /**
     * Muestra error de red con mensaje específico.
     * @param message Mensaje descriptivo del error (opcional, puede ser null)
     */
    fun showNetworkError(message: String?) {
        _currentState.value = ViewState.ERROR_NETWORK
        _errorMessage.value = message
        _showEmptyError.value = true
    }
    /**
     * Muestra error genérico con clasificación automática.
     * Determina el tipo de error y lo clasifica automáticamente.
     * @param errorType Tipo de error clasificado (usar ErrorClassifier)
     */
    fun showError(errorType: ErrorClassifier.ErrorType) {
        return when (errorType) {
            is ErrorClassifier.ErrorType.Network -> showNetworkError(errorType.details)
            is ErrorClassifier.ErrorType.Server -> showServerError(errorType.details)
            is ErrorClassifier.ErrorType.Unknown -> showServerError(errorType.details ?: "Error inesperado")
        }
    }


    /**
     * Muestra error de servidor con mensaje específico.
     * @param message Mensaje descriptivo del error (opcional, puede ser null)
     */
    fun showServerError(message: String?) {
        _currentState.value = ViewState.ERROR_SERVER
        _errorMessage.value = message
        _showEmptyError.value = true
    }

    /**
     * Muestra estado con datos (lista visible).
     * Útil cuando la carga es exitosa y hay elementos para mostrar.
     */
    fun showData() {
        _currentState.value = ViewState.DATA
        _errorMessage.value = null
        _showEmptyError.value = false
    }

    /**
     * Muestra estado vacío (sin datos pero sin error).
     * Diferente de ERROR: aquí la operación fue exitosa pero no hay elementos.
     */
    fun showEmpty() {
        _currentState.value = ViewState.EMPTY
        _errorMessage.value = null
        _showEmptyError.value = false
    }

    /**
     * Restablece todos los estados a valores iniciales.
     * Útil al recargar datos o cambiar configuración.
     */
    fun reset() {
        showLoading()
    }

    // ===== Métodos de consulta =====

    /**
     * Verifica si el estado actual es de carga.
     * @return true si está en estado LOADING, false en caso contrario
     */
    fun isLoading(): Boolean {
        return _currentState.value == ViewState.LOADING
    }

    /**
     * Verifica si el estado actual es de error.
     * @return true si está en estado ERROR_NETWORK o ERROR_SERVER
     */
    fun isError(): Boolean {
        val state = _currentState.value
        return state == ViewState.ERROR_NETWORK || state == ViewState.ERROR_SERVER
    }

    /**
     * Verifica si el estado actual muestra datos.
     * @return true si está en estado DATA, false en caso contrario
     */
    fun hasData(): Boolean {
        return _currentState.value == ViewState.DATA
    }
}
