package com.nexosolar.android.core

import android.util.Log

/**
 * Logger personalizado.
 *
 *
 * Ventajas:
 * - Los módulos data y domain no dependen de BuildConfig
 * - Los logs se desactivan automáticamente en Release
 * - Fácil migrar a logging remoto (Firebase, Sentry) sin tocar todo el código
 * - Cumple con Clean Architecture (capas puras de Kotlin)
 *
 * Inicializado en Application:
 * ```
 * Logger.isDebug = BuildConfig.DEBUG
 * ```
 */
object Logger {

    /**
     * Flag de debug. Debe ser inicializado desde el módulo app.
     */
    var isDebug: Boolean = false

    /**
     * Log de nivel DEBUG.
     * Solo se emite si isDebug = true.
     */
    fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }

    /**
     * Log de nivel INFO.
     * Solo se emite si isDebug = true.
     */
    fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
    }

    /**
     * Log de nivel WARNING.
     * Solo se emite si isDebug = true.
     */
    fun w(tag: String, message: String) {
        if (isDebug) Log.w(tag, message)
    }

    /**
     * Log de nivel ERROR.
     * Solo se emite si isDebug = true.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    /**
     * Log de nivel VERBOSE.
     * Solo se emite si isDebug = true.
     */
    fun v(tag: String, message: String) {
        if (isDebug) Log.v(tag, message)
    }
}