package org.vm.mqtt_client2

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class CamClientApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        plant(DebugTree())
    }

    companion object {
    }
}