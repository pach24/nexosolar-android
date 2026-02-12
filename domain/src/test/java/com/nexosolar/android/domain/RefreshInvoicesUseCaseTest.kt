package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.repository.InvoiceRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

@DisplayName("RefreshInvoicesUseCase - Recarga forzada de facturas")
class RefreshInvoicesUseCaseTest {

    private lateinit var mockRepository: InvoiceRepository
    private lateinit var useCase: RefreshInvoicesUseCase

    @BeforeEach
    fun setUp() {
        mockRepository = mock(InvoiceRepository::class.java)
        useCase = RefreshInvoicesUseCase(mockRepository)
    }

    @Test
    @DisplayName("Invocar el use case llama a refreshInvoices en el repositorio")
    fun `when invoked calls repository refresh`() = runTest {
        // WHEN
        useCase()

        // THEN
        verify(mockRepository, times(1)).refreshInvoices()
    }

    @Test
    @DisplayName("Si el repositorio falla, la excepción se propaga")
    fun `when repository fails exception is propagated`() = runTest {
        // GIVEN
        val errorMessage = "Server Timeout"
        `when`(mockRepository.refreshInvoices()).thenThrow(RuntimeException(errorMessage))

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            useCase()
        }

        assertEquals(errorMessage, exception.message)
    }
}
