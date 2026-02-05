package com.nexosolar.android.ui.smartsolar.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Gestor especializado en el manejo de estados de UI para la pantalla de instalación.
 */
class InstallationStateManager {

    // ===== Estados posibles de la vista =====

    enum class ViewState {
        LOADING,
        DATA,
        EMPTY,
        ERROR_NETWORK,
        ERROR_SERVER
    }

    // ===== Variables de instancia =====

    private val _currentState = MutableLiveData<ViewState>()
    val currentState: LiveData<ViewState> get() = _currentState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _showEmptyError = MutableLiveData<Boolean>()
    val showEmptyError: LiveData<Boolean> get() = _showEmptyError

    // ===== Métodos de transición de estado =====

    fun showLoading() {
        _currentState.postValue(ViewState.LOADING)
        _errorMessage.postValue(null)
        _showEmptyError.postValue(false)
    }

    fun showData() {
        _currentState.postValue(ViewState.DATA)
        _errorMessage.postValue(null)
        _showEmptyError.postValue(false)
    }

    fun showEmpty() {
        _currentState.postValue(ViewState.EMPTY)
        _errorMessage.postValue(null)
        _showEmptyError.postValue(true)
    }

    fun showNetworkError(message: String) {
        _currentState.postValue(ViewState.ERROR_NETWORK)
        _errorMessage.postValue(message)
        _showEmptyError.postValue(true)
    }

    fun showServerError(message: String) {
        _currentState.postValue(ViewState.ERROR_SERVER)
        _errorMessage.postValue(message)
        _showEmptyError.postValue(true)
    }

    // ===== Métodos de consulta =====

    fun isError(): Boolean {
        val state = _currentState.value
        return state == ViewState.ERROR_NETWORK || state == ViewState.ERROR_SERVER
    }

    fun isLoading(): Boolean {
        return _currentState.value == ViewState.LOADING
    }
}