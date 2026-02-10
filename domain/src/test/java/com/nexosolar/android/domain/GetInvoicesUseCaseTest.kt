package com.nexosolar.android.domain

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.times
import java.time.LocalDate

@DisplayName("GetInvoicesUseCase - Obtención de facturas")
class GetInvoicesUseCaseTest {

    private lateinit var mockRepository: InvoiceRepository
    private lateinit var useCase: GetInvoicesUseCase

    @BeforeEach
    fun setUp() {
        mockRepository = mock(InvoiceRepository::class.java)
        useCase = GetInvoicesUseCase(mockRepository)
    }

    @Test
    @DisplayName("Invocar el use case delega la llamada al repositorio")
    fun `when invoked delegates to repository`() = runTest {
        val mockInvoices = createMockInvoices()
        `when`(mockRepository.getInvoices()).thenReturn(mockInvoices)

        useCase()

        verify(mockRepository, times(1)).getInvoices()
    }

    @Test
    @DisplayName("Repositorio retorna datos correctamente")
    fun `when repository returns data returns invoices`() = runTest {
        val mockInvoices = createMockInvoices()
        `when`(mockRepository.getInvoices()).thenReturn(mockInvoices)

        val result = useCase()

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(100f, result[0].invoiceAmount, 0.01f)
    }

    @Test
    @DisplayName("Repositorio lanza excepción propaga el error")
    fun `when repository fails throws exception`() = runTest {
        val errorMessage = "Error de red"
        `when`(mockRepository.getInvoices()).thenThrow(RuntimeException(errorMessage))

        val exception = assertThrows<RuntimeException> {
            useCase()
        }

        assertEquals(errorMessage, exception.message)
    }

    @Test
    @DisplayName("Refresh delega la llamada al repositorio")
    fun `when refresh is called delegates to repository refresh`() = runTest {
        `when`(mockRepository.refreshInvoices()).thenReturn(Unit)

        useCase.refresh()

        verify(mockRepository, times(1)).refreshInvoices()
    }

    @Test
    @DisplayName("Lista vacía es retornada correctamente")
    fun `when repository returns empty list returns empty list`() = runTest {
        `when`(mockRepository.getInvoices()).thenReturn(emptyList())

        val result = useCase()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    private fun createMockInvoices(): List<Invoice> {
        return listOf(
            Invoice(
                invoiceID = 1,
                invoiceAmount = 100f,
                invoiceStatus = "Pagada",
                invoiceDate = LocalDate.of(2025, 1, 1)
            ),
            Invoice(
                invoiceID = 2,
                invoiceAmount = 200f,
                invoiceStatus = "Pendiente de pago",
                invoiceDate = LocalDate.of(2025, 2, 1)
            )
        )
    }
}
