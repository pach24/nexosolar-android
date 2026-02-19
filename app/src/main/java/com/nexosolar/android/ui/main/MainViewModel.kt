package com.nexosolar.android.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel de la pantalla principal.
 * Gestiona las preferencias de API (Mock/Real).
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private companion object {
        private const val PREFS_NAME = "RepoPrefs"
        private const val KEY_USE_MOCK = "last_mode_was_mock"
        private const val KEY_USE_ALT_URL = "last_url_was_alt"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    private fun loadInitialState(): MainUIState {
        return MainUIState(
            useMock = prefs.getBoolean(KEY_USE_MOCK, true),
            useAltUrl = prefs.getBoolean(KEY_USE_ALT_URL, false)
        )
    }

    /**
     * Cambia entre modo Mock y Real.
     * Si activa Mock, desactiva automáticamente URL alternativa.
     */
    fun onMockToggled(enabled: Boolean) {
        _uiState.update { currentState ->
            val newState = currentState.copy(
                useMock = enabled,
                useAltUrl = if (enabled) false else currentState.useAltUrl
            )
            savePreferences(newState)
            newState
        }
    }

    /**
     * Cambia URL alternativa (solo si modo Real está activo).
     */
    fun onAltUrlToggled(enabled: Boolean) {
        if (_uiState.value.useMock) return

        _uiState.update { currentState ->
            val newState = currentState.copy(useAltUrl = enabled)
            savePreferences(newState)
            newState
        }
    }

    private fun savePreferences(state: MainUIState) {
        prefs.edit().apply {
            putBoolean(KEY_USE_MOCK, state.useMock)
            putBoolean(KEY_USE_ALT_URL, state.useAltUrl)
            apply()
        }
    }
}
