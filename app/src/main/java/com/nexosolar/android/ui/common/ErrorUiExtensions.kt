package com.nexosolar.android.ui.common

import com.nexosolar.android.R
import com.nexosolar.android.core.ErrorClassifier

/**
 * Extension function para obtener el ID del recurso de texto.
 * Devuelve Int (@StringRes) para ser usado con stringResource() en Compose.
 */
fun ErrorClassifier.ErrorType.toUserMessageRes(): Int {
    return when (this) {
        is ErrorClassifier.ErrorType.Network -> R.string.error_conexion_description_message
        is ErrorClassifier.ErrorType.Server -> R.string.error_conexion_servidor_description_message
        is ErrorClassifier.ErrorType.Unknown -> R.string.error_message_generic
    }
}
