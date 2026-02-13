package com.nexosolar.android.domain

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.repository.InvoiceRepository
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

@DisplayName("GetInvoicesUseCase - Obtención de facturas (Flow)")
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
        // GIVEN
        val mockFlow = flowOf(createMockInvoices())
        `when`(mockRepository.getInvoices()).thenReturn(mockFlow)

        // WHEN
        val result = useCase() // Asignamos el resultado

        // THEN
        verify(mockRepository, times(1)).getInvoices()
        assertEquals(mockFlow, result) // Verificamos que es el correcto
    }


    @Test
    @DisplayName("Repositorio emite datos correctamente")
    fun `when repository emits data usecase flow emits invoices`() = runTest {
        // GIVEN
        val mockInvoices = createMockInvoices()
        `when`(mockRepository.getInvoices()).thenReturn(flowOf(mockInvoices))

        // WHEN
        // Usamos .first() para obtener el primer valor emitido por el Flow
        val result = useCase().first()

        // THEN
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(100f, result[0].invoiceAmount, 0.01f)
    }

    @Test
    @DisplayName("Excepción en el flow se propaga correctamente")
    fun `when flow throws exception it is propagated`() = runTest {
        // GIVEN
        val errorMessage = "Error de red"
        // Simulamos un flow que lanza error al ser recolectado
        `when`(mockRepository.getInvoices()).thenReturn(flow {
            throw RuntimeException(errorMessage)
        })

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            useCase().first()
        }

        assertEquals(errorMessage, exception.message)
    }

    @Test
    @DisplayName("Lista vacía es retornada correctamente")
    fun `when repository returns empty list returns empty list`() = runTest {
        // GIVEN
        `when`(mockRepository.getInvoices()).thenReturn(flowOf(emptyList()))

        // WHEN
        val result = useCase().first()

        // THEN
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    // Método helper para crear datos de prueba (basado en TU modelo actual)
    private fun createMockInvoices(): List<Invoice> {
        return listOf(
            Invoice(
                invoiceID = 1,
                invoiceAmount = 100f,
                invoiceStatus = "Pagada",
                invoiceDate = LocalDate.of(2025, 1, 1)
                // Eliminados campos extra que no tienes
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
