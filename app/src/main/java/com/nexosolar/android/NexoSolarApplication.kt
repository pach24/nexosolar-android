package com.nexosolar.android

import android.app.Application
import com.nexosolar.android.core.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NexoSolarApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.isDebug = BuildConfig.DEBUG
    }
}
