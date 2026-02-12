package com.nexosolar.android.data

import android.content.Context
import android.content.SharedPreferences
import com.nexosolar.android.data.util.Logger
import com.nexosolar.android.data.local.AppDatabase
import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.remote.ApiClientManager
import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.data.repository.InstallationRepositoryImpl
import com.nexosolar.android.data.repository.InvoiceRepositoryImpl
import com.nexosolar.android.data.source.InvoiceRemoteDataSourceImpl
import com.nexosolar.android.domain.repository.InstallationRepository
import com.nexosolar.android.domain.repository.InvoiceRepository
import com.nexosolar.android.data.source.InvoiceLocalDataSourceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import androidx.core.content.edit
import com.nexosolar.android.data.source.InstallationLocalDataSourceImpl
import com.nexosolar.android.data.source.InstallationRemoteDataSourceImpl

/**
 * Módulo de inyección de dependencias manual para la capa de datos.
 *
 * Responsabilidades:
 * - Proveer instancias configuradas de repositorios
 * - Gestionar el cambio entre modo Mock (Retromock) y Real (Retrofit)
 * - Limpiar caché local cuando cambia la configuración de API
 * - Coordinar dependencias entre Room, Retrofit y repositorios
 *
 * Estrategia de caché:
 * - Si cambia el modo Mock ↔ Real: limpia la base de datos
 * - Si cambia la URL (en modo Real): limpia la base de datos
 * - Persiste la configuración en SharedPreferences para detectar cambios
 */
class DataModule(
    context: Context,
    private val useMock: Boolean,
    private val useAlternativeUrl: Boolean
) {

    private val context: Context = context.applicationContext

    private companion object {
        private const val TAG = "DataModule"
        private const val PREFS_NAME = "RepoPrefs"
        private const val KEY_LAST_MODE_WAS_MOCK = "last_mode_was_mock"
        private const val KEY_LAST_URL_WAS_ALT = "last_url_was_alt"
    }

    /**
     * Proporciona una instancia configurada del repositorio de facturas.
     *
     * Detecta cambios de configuración (Mock/Real o URL) y limpia la base de datos
     * si es necesario para evitar inconsistencias entre diferentes fuentes de datos.
     *
     * @return Repositorio de facturas configurado según el modo actual
     */
    fun provideInvoiceRepository(): InvoiceRepository {
        val apiService = provideApiService()
        val invoiceDao = provideInvoiceDao()
        val prefs = provideSharedPrefs()

        // Detectar cualquier cambio de configuración (Mock o URL)
        val lastModeWasMock = prefs.getBoolean(KEY_LAST_MODE_WAS_MOCK, false)
        val lastUrlWasAlt = prefs.getBoolean(KEY_LAST_URL_WAS_ALT, false)

        // Si cambió el modo Mock O si cambió la URL (estando en modo real)
        val configChanged = (useMock != lastModeWasMock) ||
                (!useMock && useAlternativeUrl != lastUrlWasAlt)

        if (configChanged) {
            Logger.w(TAG, "[CONFIG] Configuration changed: Mock=$useMock, AltUrl=$useAlternativeUrl")
            clearDatabaseOnModeSwitch(invoiceDao)

            // Guardar nueva configuración
            prefs.edit {
                putBoolean(KEY_LAST_MODE_WAS_MOCK, useMock)
                    .putBoolean(KEY_LAST_URL_WAS_ALT, useAlternativeUrl)
            }
        }

        val remoteDataSource = InvoiceRemoteDataSourceImpl(apiService)
        val localDataSource = InvoiceLocalDataSourceImpl(invoiceDao)
        val mapper = InvoiceMapper

        return InvoiceRepositoryImpl(remoteDataSource, localDataSource, mapper, isMockMode = useMock)
    }

    /**
     * Proporciona una instancia del repositorio de instalaciones.
     */
    fun provideInstallationRepository(): InstallationRepository {
        val apiService = provideApiService()
        val database = AppDatabase.getInstance(context) // Obtenemos la BD
        val installationDao = database.installationDao() // Obtenemos el DAO

        // 1. Data Sources
        val remoteDataSource = InstallationRemoteDataSourceImpl(apiService)
        val localDataSource = InstallationLocalDataSourceImpl(installationDao)
        val mapper = InstallationMapper

        // 2. Repositorio con dependencias inyectadas
        return InstallationRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            mapper = mapper
        )
    }

    /**
     * Proporciona el servicio de API configurado según el modo y URL actuales.
     *
     * @return Instancia de ApiService (Mock o Real según configuración)
     */
    private fun provideApiService(): ApiService {
        ApiClientManager.init(context)
        return ApiClientManager.getApiService(useMock, useAlternativeUrl)
    }

    /**
     * Proporciona acceso al DAO de facturas desde Room.
     *
     * @return DAO de facturas para operaciones de base de datos
     */
    private fun provideInvoiceDao(): InvoiceDao {
        return AppDatabase.getInstance(context).invoiceDao()
    }

    /**
     * Proporciona acceso a SharedPreferences para persistir configuración.
     *
     * @return Instancia de SharedPreferences
     */
    private fun provideSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Limpia la base de datos local de forma síncrona cuando cambia la configuración.
     *
     * Usa runBlocking para ejecutar la operación suspend de forma síncrona,
     * garantizando que se complete antes de continuar con la nueva configuración.
     *
     * @param invoiceDao DAO para ejecutar la operación de limpieza
     */
    private fun clearDatabaseOnModeSwitch(invoiceDao: InvoiceDao) {
        Logger.w(TAG, "[DATABASE] Clearing Room due to configuration change")

        try {
            runBlocking(Dispatchers.IO) {
                invoiceDao.deleteAll()
            }
            Logger.w(TAG, "[DATABASE] Room cleared successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "[DATABASE] Error clearing Room: ${e.message}", e)
        }
    }
}
