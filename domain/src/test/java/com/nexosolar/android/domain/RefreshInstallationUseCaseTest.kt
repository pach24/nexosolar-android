package com.nexosolar.android.domain.usecase.installation

import com.nexosolar.android.domain.repository.InstallationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

@DisplayName("RefreshInstallationUseCase - Recarga forzada de instalación")
class RefreshInstallationUseCaseTest {

    private lateinit var mockRepository: InstallationRepository
    private lateinit var useCase: RefreshInstallationUseCase

    @BeforeEach
    fun setUp() {
        mockRepository = mock(InstallationRepository::class.java)
        useCase = RefreshInstallationUseCase(mockRepository)
    }

    @Test
    @DisplayName("Invocar el use case llama a refreshInstallation en el repositorio")
    fun `when invoked calls repository refresh`() = runTest {
        // WHEN
        useCase()

        // THEN
        verify(mockRepository, times(1)).refreshInstallation()
    }

    @Test
    @DisplayName("Si el repositorio falla, la excepción se propaga")
    fun `when repository fails exception is propagated`() = runTest {
        // GIVEN
        val errorMessage = "Error de red 404"
        // Simulamos un error en la función suspendida
        `when`(mockRepository.refreshInstallation()).thenThrow(RuntimeException(errorMessage))

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            useCase()
        }

        assertEquals(errorMessage, exception.message)
    }
}
