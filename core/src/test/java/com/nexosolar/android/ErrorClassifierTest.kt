package com.nexosolar.android

import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.toTechnicalMessage
import com.nexosolar.android.core.toUserMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Tests unitarios para [com.nexosolar.android.core.ErrorClassifier].
 * Valida la lógica de clasificación de errores según tipo de excepción.
 */
class ErrorClassifierTest {

    // ========== Tests para classify(Throwable) ==========

    @Test
    fun `classify returns Unknown when error is null`() {
        // WHEN
        val result = ErrorClassifier.classify(null)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Unknown,
            "Un error nulo debería clasificarse como Unknown")
    }

    @Test
    fun `classify returns Server for SocketTimeoutException`() {
        // GIVEN
        val error = SocketTimeoutException("Connection timed out")

        // WHEN
        val result = ErrorClassifier.classify(error)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Server,
            "Un timeout debería clasificarse como error de Server")
        assertEquals("Connection timed out", (result as ErrorClassifier.ErrorType.Server).details)
    }

    @Test
    fun `classify returns Network for UnknownHostException`() {
        // GIVEN
        val error = UnknownHostException("Unable to resolve host")

        // WHEN
        val result = ErrorClassifier.classify(error)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Network,
            "UnknownHostException debería clasificarse como error de Network")
    }

    @Test
    fun `classify returns Network for generic IOException`() {
        // GIVEN
        val error = IOException("Network is unreachable")

        // WHEN
        val result = ErrorClassifier.classify(error)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Network,
            "IOException genérica debería clasificarse como error de Network")
        assertEquals("Network is unreachable", (result as ErrorClassifier.ErrorType.Network).details)
    }

    @Test
    fun `classify returns Unknown for RuntimeException`() {
        // GIVEN
        val error = RuntimeException("Unexpected error")

        // WHEN
        val result = ErrorClassifier.classify(error)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Unknown,
            "RuntimeException no relacionada con red debería ser Unknown")
        assertEquals("Unexpected error", (result as ErrorClassifier.ErrorType.Unknown).details)
    }

    // ========== Tests para classifyHttp(int) ==========

    @Test
    fun `classifyHttp returns Server for HTTP 500`() {
        // WHEN
        val result = ErrorClassifier.classifyHttp(500)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Server,
            "Código HTTP 500 debería clasificarse como error de Server")
        assertEquals("HTTP 500", (result as ErrorClassifier.ErrorType.Server).details)
    }

    @Test
    fun `classifyHttp returns Server for HTTP 503`() {
        // WHEN
        val result = ErrorClassifier.classifyHttp(503)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Server,
            "Código HTTP 503 debería clasificarse como error de Server")
        assertEquals("HTTP 503", (result as ErrorClassifier.ErrorType.Server).details)
    }

    @Test
    fun `classifyHttp returns Server for HTTP 404`() {
        // WHEN
        val result = ErrorClassifier.classifyHttp(404)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Server,
            "Código HTTP 404 debería clasificarse como error de Server")
        assertEquals("HTTP 404", (result as ErrorClassifier.ErrorType.Server).details)
    }

    @Test
    fun `classifyHttp returns Server for HTTP 401`() {
        // WHEN
        val result = ErrorClassifier.classifyHttp(401)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Server,
            "Código HTTP 401 debería clasificarse como error de Server")
        assertEquals("HTTP 401", (result as ErrorClassifier.ErrorType.Server).details)
    }

    @Test
    fun `classifyHttp returns Unknown for HTTP 200`() {
        // WHEN
        val result = ErrorClassifier.classifyHttp(200)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Unknown,
            "Código HTTP 200 no es un error, debería ser Unknown")
    }

    @Test
    fun `classifyHttp returns Unknown for HTTP 301`() {
        // WHEN
        val result = ErrorClassifier.classifyHttp(301)

        // THEN
        assertTrue(result is ErrorClassifier.ErrorType.Unknown,
            "Código HTTP 301 debería clasificarse como Unknown")
    }

    // ========== Tests para extension functions ==========

    @Test
    fun `toUserMessage returns correct message for Network error`() {
        // GIVEN
        val error = ErrorClassifier.ErrorType.Network("Connection refused")

        // WHEN
        val message = error.toUserMessage()

        // THEN
        assertEquals("No hay conexión a internet. Revisa tu red.", message,
            "El mensaje para Network debería mencionar la conexión")
    }

    @Test
    fun `toUserMessage returns correct message for Server error`() {
        // GIVEN
        val error = ErrorClassifier.ErrorType.Server("HTTP 500")

        // WHEN
        val message = error.toUserMessage()

        // THEN
        assertTrue(message.contains("HTTP 500"),
            "El mensaje para Server debería incluir los detalles")
        assertTrue(message.contains("servidor"),
            "El mensaje debería mencionar el servidor")
    }

    @Test
    fun `toUserMessage includes error details for Unknown error`() {
        // GIVEN
        val error = ErrorClassifier.ErrorType.Unknown("Custom error details")

        // WHEN
        val message = error.toUserMessage()

        // THEN
        assertTrue(message.contains("Custom error details"),
            "El mensaje debería incluir los detalles de la excepción")
    }

    @Test
    fun `toUserMessage handles null details gracefully`() {
        // GIVEN
        val error = ErrorClassifier.ErrorType.Unknown(null)

        // WHEN
        val message = error.toUserMessage()

        // THEN
        assertEquals("Error inesperado.", message,
            "Debería manejar detalles nulos con mensaje por defecto")
    }

    @Test
    fun `toTechnicalMessage returns formatted message for Network`() {
        // GIVEN
        val error = ErrorClassifier.ErrorType.Network("Timeout after 30s")

        // WHEN
        val message = error.toTechnicalMessage()

        // THEN
        assertEquals("Network error: Timeout after 30s", message)
    }

    @Test
    fun `toTechnicalMessage handles null details`() {
        // GIVEN
        val error = ErrorClassifier.ErrorType.Server(null)

        // WHEN
        val message = error.toTechnicalMessage()

        // THEN
        assertEquals("Server error: Unknown server issue", message)
    }
}
