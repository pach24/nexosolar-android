package com.nexosolar.android.domain

import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

/**
 * Tests unitarios para GetInstallationDetailsUseCase.
 * Valida la interacción con el repositorio usando corrutinas.
 */
@DisplayName("GetInstallationDetailsUseCase - Obtención de detalles de instalación")
class GetInstallationDetailsUseCaseTest {

    private lateinit var mockRepository: InstallationRepository
    private lateinit var useCase: GetInstallationDetailsUseCase

    @BeforeEach
    fun setUp() {
        mockRepository = mock(InstallationRepository::class.java)
        useCase = GetInstallationDetailsUseCase(mockRepository)
    }

    @Test
    @DisplayName("Invocar el use case delega la llamada al repositorio")
    fun `when invoked delegates to repository`() = runTest {
        // GIVEN: Repositorio configurado para retornar datos
        val mockInstallation = createMockInstallation()
        `when`(mockRepository.getInstallationDetails()).thenReturn(mockInstallation)

        // WHEN: Invocamos el use case
        useCase()

        // THEN: Verifica que el repositorio fue llamado exactamente una vez
        verify(mockRepository, times(1)).getInstallationDetails()
    }

    @Test
    @DisplayName("Repositorio retorna datos correctamente")
    fun `when repository returns data returns installation details`() = runTest {
        // GIVEN: Repositorio configurado para retornar datos exitosamente
        val mockInstallation = createMockInstallation()
        `when`(mockRepository.getInstallationDetails()).thenReturn(mockInstallation)

        // WHEN: Invocamos el use case
        val result = useCase()

        // THEN: Verifica que se reciben los datos correctos
        assertNotNull(result)
        assertEquals("ES0021000000000001JN", result.selfConsumptionCode)
        assertEquals("Activa", result.installationStatus)
        assertEquals("Residencial", result.installationType)
        assertEquals("Con compensación", result.compensation)
        assertEquals("5.5 kW", result.power)
    }

    @Test
    @DisplayName("Repositorio lanza excepción propaga el error")
    fun `when repository fails throws exception`() = runTest {
        // GIVEN: Repositorio configurado para lanzar error
        val errorMessage = "Error de red"
        `when`(mockRepository.getInstallationDetails())
            .thenThrow(RuntimeException(errorMessage))

        // WHEN & THEN: Verifica que la excepción se propaga
        val exception = assertThrows<RuntimeException> {
            useCase()
        }

        assertEquals(errorMessage, exception.message)
    }

    @Test
    @DisplayName("Llamadas múltiples al use case ejecutan el repositorio cada vez")
    fun `when called multiple times repository is invoked each time`() = runTest {
        // GIVEN: Repositorio configurado
        val mockInstallation = createMockInstallation()
        `when`(mockRepository.getInstallationDetails()).thenReturn(mockInstallation)

        // WHEN: Llamamos dos veces
        useCase()
        useCase()

        // THEN: El repositorio fue llamado dos veces (sin caché en UseCase)
        verify(mockRepository, times(2)).getInstallationDetails()
    }

    @Test
    @DisplayName("Repositorio retorna instalación con campos nulos es válido")
    fun `when repository returns installation with null fields is valid`() = runTest {
        // GIVEN: Instalación con algunos campos null
        val installationWithNulls = Installation().apply {
            selfConsumptionCode = "ES123"
            // Resto de campos null
        }
        `when`(mockRepository.getInstallationDetails()).thenReturn(installationWithNulls)

        // WHEN: Invocamos el use case
        val result = useCase()

        // THEN: Acepta instalaciones con campos null
        assertNotNull(result)
        assertEquals("ES123", result.selfConsumptionCode)
        assertNull(result.installationStatus)
        assertNull(result.compensation)
    }

    // ========== Métodos auxiliares ==========

    /**
     * Crea una instalación mock completa para pruebas.
     */
    private fun createMockInstallation(): Installation {
        return Installation(
            cau = "ES0021000000000001JN",
            status = "Activa",
            type = "Residencial",
            compensation = "Con compensación",
            power = "5.5 kW"
        )
    }
}
