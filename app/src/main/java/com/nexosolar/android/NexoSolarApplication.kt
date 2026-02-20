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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltAndroidApp
class NexoSolarApplication : Application() {

    private val warmUpScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Logger.isDebug = BuildConfig.DEBUG

        // Programamos el warmup cuando el loop principal quede idle para no competir
        // con el primer draw de MainActivity durante cold start.
        Looper.getMainLooper().queue.addIdleHandler(
            MessageQueue.IdleHandler {
                warmUpScope.launch {
                    // Pequeña espera para evitar contención CPU justo tras el primer frame.
                    delay(400)

                    ApiClientManager.init(this@NexoSolarApplication)

                    // Precalentamos dependencias fuera de UI. Se mantiene en background
                    // para amortizar el coste de primer uso sin bloquear arranque.
                    ApiClientManager.getApiService(useMock = true, useAlternativeUrl = false)
                    AppDatabase.getInstance(this@NexoSolarApplication).openHelper.writableDatabase
                }
                false
            }
        )
    }
}
