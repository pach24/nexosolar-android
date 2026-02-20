package com.nexosolar.android

import android.app.Application
import android.os.Looper
import android.os.MessageQueue
import com.nexosolar.android.core.Logger
import com.nexosolar.android.data.local.AppDatabase
import com.nexosolar.android.data.remote.ApiClientManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class NexoSolarApplication : Application() {

    private val warmUpScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Logger.isDebug = BuildConfig.DEBUG

        warmUpScope.launch {
            // Precalentamos inicializaciones costosas para evitar jank
            // al abrir por primera vez pantallas con Room/Retrofit.
            ApiClientManager.init(this@NexoSolarApplication)
            ApiClientManager.getApiService(useMock = true, useAlternativeUrl = false)
            AppDatabase.getInstance(this@NexoSolarApplication).openHelper.writableDatabase
        }
    }
}
