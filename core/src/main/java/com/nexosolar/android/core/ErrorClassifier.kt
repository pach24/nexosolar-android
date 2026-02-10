package com.nexosolar.android.core

import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Clasificador de errores con tipo seguro.
 *
 * Analiza excepciones y códigos HTTP para determinar el tipo de error
 * (red, servidor, desconocido) y proporcionar mensajes apropiados.
 */
object ErrorClassifier {

    /**
     * Clasifica una excepción según su tipo y origen.
     *
     * @param error Excepción a analizar
     * @return Tipo de error clasificado
     */
    fun classify(error: Throwable?): ErrorType {
        if (error == null) return ErrorType.Unknown(null)

        val className = error.javaClass.name

        return when {
            // Errores de servidor/timeout
            className.contains("HttpException") ||
                    error is SocketTimeoutException ||
                    className.contains("SocketTimeoutException") -> ErrorType.Server(error.message)

            // Errores de red (sin conexión)
            error is IOException -> ErrorType.Network(error.message)

            // Desconocido
            else -> ErrorType.Unknown(error.message)
        }
    }

    /**
     * Clasifica un error según el código HTTP.
     *
     * @param code Código de respuesta HTTP
     * @return Tipo de error clasificado
     */
    fun classifyHttp(code: Int): ErrorType = when {
        code >= 500 -> ErrorType.Server("HTTP $code")
        code >= 400 -> ErrorType.Server("HTTP $code")
        else -> ErrorType.Unknown(null)
    }

    /**
     * Tipos de error con información contextual.
     */
    sealed class ErrorType {
        /**
         * Error de conexión a internet.
         *
         * @property details Detalles técnicos del error (opcional)
         */
        data class Network(val details: String? = null) : ErrorType()

        /**
         * Error del servidor o timeout.
         *
         * @property details Detalles técnicos del error (opcional)
         */
        data class Server(val details: String? = null) : ErrorType()

        /**
         * Error desconocido o no clasificado.
         *
         * @property details Detalles técnicos del error (opcional)
         */
        data class Unknown(val details: String? = null) : ErrorType()
    }
}

/**
 * Extension function para obtener mensaje de error user-friendly.
 *
 * Separa la lógica de presentación del clasificador.
 */
fun ErrorClassifier.ErrorType.toUserMessage(): String = when (this) {
    is ErrorClassifier.ErrorType.Network ->
        "No hay conexión a internet. Revisa tu red."

    is ErrorClassifier.ErrorType.Server ->
        "El servidor no responde correctamente${details?.let { " ($it)" }.orEmpty()}."

    is ErrorClassifier.ErrorType.Unknown ->
        "Error inesperado${details?.let { ": $it" }.orEmpty()}."
}

/**
 * Extension function para logging técnico.
 */
fun ErrorClassifier.ErrorType.toTechnicalMessage(): String = when (this) {
    is ErrorClassifier.ErrorType.Network -> "Network error: ${details ?: "No connection"}"
    is ErrorClassifier.ErrorType.Server -> "Server error: ${details ?: "Unknown server issue"}"
    is ErrorClassifier.ErrorType.Unknown -> "Unknown error: ${details ?: "No details"}"
}
