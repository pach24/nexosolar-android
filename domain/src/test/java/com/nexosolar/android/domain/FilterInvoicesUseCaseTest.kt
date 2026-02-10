package com.nexosolar.android.domain

import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Tests unitarios para FilterInvoicesUseCase.
 * Valida la lógica de filtrado de facturas por estado, fecha e importe.
 */
@DisplayName("FilterInvoicesUseCase - Filtrado de facturas")
class FilterInvoicesUseCaseTest {

    private lateinit var useCase: FilterInvoicesUseCase
    private lateinit var baseList: List<Invoice>

    @BeforeEach
    fun setUp() {
        useCase = FilterInvoicesUseCase()

        // Datos base de prueba
        baseList = listOf(
            createInvoice(amount = 100f, status = "Pagada", date = LocalDate.of(2025, 1, 1)),
            createInvoice(amount = 200f, status = "Pendiente de pago", date = LocalDate.of(2025, 2, 1)),
            createInvoice(amount = 300f, status = "Anulada", date = LocalDate.of(2025, 3, 1))
        )
    }

    @Test
    @DisplayName("Filtrar por estado retorna solo facturas del estado especificado")
    fun `when filter by status returns only matching invoices`() {
        // GIVEN: Lista de facturas con diferentes estados
        val statuses = listOf("Pagada")

        // WHEN: Filtramos por estado "Pagada" usando el operador invoke
        val result = useCase(
            invoices = baseList,
            statusList = statuses
        )

        // THEN: Solo retorna facturas con estado "Pagada"
        assertEquals(1, result.size, "Debería retornar solo 1 factura con estado Pagada")
        assertEquals("Pagada", result[0].invoiceStatus)
    }

    @Test
    @DisplayName("Filtrar por rango de importe retorna facturas dentro del rango")
    fun `when filter by amount range returns invoices in range`() {
        // GIVEN: Lista de facturas con diferentes importes (100, 200, 300)

        // WHEN: Filtramos por rango de importe entre 150 y 250 usando rangos de Kotlin
        val result = useCase(
            invoices = baseList,
            amountRange = 150f..250f
        )

        // THEN: Solo retorna la factura de 200 (única dentro del rango)
        assertEquals(1, result.size, "Debería retornar solo 1 factura en el rango 150-250")
        assertEquals(200f, result[0].invoiceAmount, 0.01f)
    }

    @Test
    @DisplayName("Sin filtros retorna todas las facturas")
    fun `when no filters returns all invoices`() {
        // GIVEN: Lista de facturas sin ningún filtro aplicado

        // WHEN: Ejecutamos sin filtros (usando parámetros por defecto)
        val result = useCase(invoices = baseList)

        // THEN: Retorna todas las facturas
        assertEquals(3, result.size, "Sin filtros debería retornar todas las facturas")
    }

    @Test
    @DisplayName("Lista vacía retorna lista vacía")
    fun `when empty list returns empty list`() {
        // GIVEN: Lista vacía de facturas
        val emptyList = emptyList<Invoice>()
        val statuses = listOf("Pagada")

        // WHEN: Filtramos una lista vacía
        val result = useCase(
            invoices = emptyList,
            statusList = statuses
        )

        // THEN: Retorna lista vacía
        assertEquals(0, result.size, "Una lista vacía debería retornar lista vacía")
    }

    @Test
    @DisplayName("Filtrar por rango de fechas retorna facturas dentro del periodo")
    fun `when filter by date returns invoices in date range`() {
        // GIVEN: Lista de facturas con diferentes fechas
        val startDate = LocalDate.of(2025, 1, 15)
        val endDate = LocalDate.of(2025, 2, 15)

        // WHEN: Filtramos por rango de fechas usando rangos de Kotlin
        val result = useCase(
            invoices = baseList,
            dateRange = startDate..endDate
        )

        // THEN: Solo retorna facturas dentro del rango de fechas
        assertEquals(1, result.size, "Debería retornar solo facturas en el rango de fechas")
        assertEquals(LocalDate.of(2025, 2, 1), result[0].invoiceDate)
    }

    @Test
    @DisplayName("Múltiples filtros simultáneos aplican lógica AND")
    fun `when multiple filters returns invoices meeting all criteria`() {
        // GIVEN: Lista de facturas y múltiples filtros simultáneos
        val statuses = listOf("Pagada")
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        // WHEN: Aplicamos múltiples filtros (estado + fecha + importe)
        val result = useCase(
            invoices = baseList,
            statusList = statuses,
            dateRange = startDate..endDate,
            amountRange = 50f..150f
        )

        // THEN: Solo retorna facturas que cumplen TODOS los criterios
        assertEquals(1, result.size, "Debería retornar facturas que cumplen todos los filtros")

        with(result[0]) {
            assertEquals("Pagada", invoiceStatus)
            assertEquals(100f, invoiceAmount, 0.01f)
            assertEquals(LocalDate.of(2025, 1, 1), invoiceDate)
        }
    }

    @Test
    @DisplayName("Filtrar por múltiples estados retorna facturas que coincidan con cualquiera")
    fun `when filter by multiple statuses returns matching invoices`() {
        // GIVEN: Lista de facturas con diferentes estados
        val statuses = listOf("Pagada", "Anulada")

        // WHEN: Filtramos por múltiples estados
        val result = useCase(
            invoices = baseList,
            statusList = statuses
        )

        // THEN: Retorna facturas con cualquiera de los estados especificados
        assertEquals(2, result.size, "Debería retornar 2 facturas (Pagada y Anulada)")
    }

    @Test
    @DisplayName("Facturas sin fecha son excluidas al filtrar por rango de fechas")
    fun `when filter by date range invoices without date are excluded`() {
        // GIVEN: Lista con una factura sin fecha
        val listWithNullDate = baseList + createInvoice(
            amount = 150f,
            status = "Pagada",
            date = null
        )

        // WHEN: Filtramos por rango de fechas
        val result = useCase(
            invoices = listWithNullDate,
            dateRange = LocalDate.of(2025, 1, 1)..LocalDate.of(2025, 12, 31)
        )

        // THEN: La factura sin fecha NO aparece en los resultados
        assertEquals(3, result.size, "Solo las facturas con fecha válida deben aparecer")
        result.forEach { invoice ->
            assert(invoice.invoiceDate != null) { "Todas las facturas deberían tener fecha" }
        }
    }

    @Test
    @DisplayName("Rangos de importes con límites exactos incluyen facturas en los bordes")
    fun `when amount equals range boundary is included`() {
        // GIVEN: Factura con importe exacto en el límite

        // WHEN: Filtramos con rango que incluye el valor exacto
        val result = useCase(
            invoices = baseList,
            amountRange = 100f..200f
        )

        // THEN: Incluye facturas en los bordes (100 y 200)
        assertEquals(2, result.size)
        assert(result.any { it.invoiceAmount == 100f })
        assert(result.any { it.invoiceAmount == 200f })
    }

    // ========== Métodos auxiliares ==========

    /**
     * Crea una factura de prueba con los parámetros dados.
     */
    private fun createInvoice(
        amount: Float,
        status: String,
        date: LocalDate?
    ): Invoice {
        return Invoice(
            invoiceID = amount.toInt(),
            invoiceAmount = amount,
            invoiceStatus = status,
            invoiceDate = date
        )
    }
}
