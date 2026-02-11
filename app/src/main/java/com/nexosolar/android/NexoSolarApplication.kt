package com.nexosolar.android

import android.app.Application
import com.nexosolar.android.data.DataModule
import com.nexosolar.android.data.util.Logger

/**
 * Clase Application principal de la app.
 * Responsable de inicializar y mantener el DataModule global.
 */
class NexoSolarApplication : Application() {

    // ===== Variables de instancia =====

    lateinit var dataModule: DataModule
        private set

    // ===== Métodos del ciclo de vida =====

    override fun onCreate() {
        super.onCreate()
        // Por defecto: Mock activado, URL Alternativa desactivada
        dataModule = DataModule(this, true, false)
        Logger.isDebug = BuildConfig.DEBUG
    }

    // ===== Métodos públicos =====

    /**
     * Permite cambiar la configuración del DataModule en tiempo de ejecución.
     * @param useMock true para usar datos mock, false para datos reales
     * @param useAlternativeUrl true para usar URL alternativa, false para URL por defecto
     */
    fun switchDataModule(useMock: Boolean, useAlternativeUrl: Boolean) {
        dataModule = DataModule(this, useMock, useAlternativeUrl)
    }
}