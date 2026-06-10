package com.mowalk.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mowalk.app.di.ServiceLocator

class MoWalkApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ServiceLocator.initialize(this)

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        try {
            if (!WorkManager.isInitialized()) {
                WorkManager.initialize(this, config)
            }
        } catch (e: Exception) {
            Log.w(TAG, "WorkManager initialization failed, using default: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "MoWalkApplication"
    }
}
