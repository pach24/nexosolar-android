package com.nexosolar.android.domain

import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.domain.repository.InstallationRepository
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

@DisplayName("GetInstallationDetailsUseCase - Obtención de detalles de instalación (Flow)")
class GetInstallationDetailsUseCaseTest {

    private lateinit var mockRepository: InstallationRepository
    private lateinit var useCase: GetInstallationDetailsUseCase

    @BeforeEach
    fun setUp() {
        mockRepository = mock(InstallationRepository::class.java)
        useCase = GetInstallationDetailsUseCase(mockRepository)
    }

    @Test
    @DisplayName("Invocar el use case retorna el Flow del repositorio")
    fun `when invoked delegates to repository`() = runTest {
        // GIVEN: Repositorio retorna un Flow con datos
        val mockInstallation = createMockInstallation()
        `when`(mockRepository.getInstallationDetails()).thenReturn(flowOf(mockInstallation))

        // WHEN: Invocamos el use case
        val flowResult = useCase()

        // THEN: Verificamos que obtenemos el dato al recolectar
        val result = flowResult.first() // first() suspende hasta recibir el dato
        assertNotNull(result)
        verify(mockRepository, times(1)).getInstallationDetails()
    }

    @Test
    @DisplayName("Repositorio retorna datos correctamente a través del Flow")
    fun `when repository emits data use case propagates it`() = runTest {
        // GIVEN
        val mockInstallation = createMockInstallation()
        `when`(mockRepository.getInstallationDetails()).thenReturn(flowOf(mockInstallation))

        // WHEN
        val result = useCase().first()

        // THEN
        assertNotNull(result)
        assertEquals("ES0021000000000001JN", result?.selfConsumptionCode)
        assertEquals("Activa", result?.installationStatus)
    }

    @Test
    @DisplayName("Si el Flow del repositorio lanza error, el UseCase lo propaga")
    fun `when repository flow fails throws exception`() = runTest {
        // GIVEN: Un Flow que emite un error
        val errorMessage = "Error de base de datos"
        `when`(mockRepository.getInstallationDetails()).thenReturn(flow {
            throw RuntimeException(errorMessage)
        })

        // WHEN & THEN: Al intentar recolectar (.first), salta la excepción
        val exception = assertThrows<RuntimeException> {
            useCase().first()
        }

        assertEquals(errorMessage, exception.message)
    }

    @Test
    @DisplayName("Repositorio puede retornar null (tabla vacía)")
    fun `when repository returns null use case emits null`() = runTest {
        // GIVEN: Repositorio devuelve Flow con null
        `when`(mockRepository.getInstallationDetails()).thenReturn(flowOf(null))

        // WHEN
        val result = useCase().first()

        // THEN
        assertNull(result)
    }

    // ========== Métodos auxiliares ==========

    private fun createMockInstallation(): Installation {
        return Installation(
            selfConsumptionCode = "ES0021000000000001JN",
            installationStatus = "Activa",
            installationType = "Residencial",
            compensation = "Con compensación",
            power = "5.5 kW"
        )
    }
}
