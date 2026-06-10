package com.mowalk.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mowalk.app.data.local.LocalDataSource
import com.mowalk.app.data.local.MoWalkDatabase
import com.mowalk.app.data.sensor.StepDeltaCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val serviceIntent = Intent(context, StepCounterService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        CoroutineScope(Dispatchers.IO).launch {
            val database = MoWalkDatabase.getDatabase(context)
            val localDataSource = LocalDataSource(database.stepDao())
            val calculator = StepDeltaCalculator()

            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val yesterdayEntity = localDataSource.getByDate(yesterday)
            val yesterdayTotal = yesterdayEntity?.steps ?: 0

            calculator.resetForNewDay(yesterdayTotal)
            localDataSource.upsertTodaySteps(0, 0f, 0f)
        }
    }
}
