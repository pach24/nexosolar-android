package com.nexosolar.android.ui.main

/**
 * Estado de la pantalla principal.
 */
data class MainUIState(
    val userName: String = "USUARIO",
    val userAddress: String = "Avenida de la Constitución 45",
    val useMock: Boolean = true,
    val useAltUrl: Boolean = false
)
