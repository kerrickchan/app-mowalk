package com.mowalk.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mowalk.app.R
import com.mowalk.app.data.local.LocalDataSource
import com.mowalk.app.data.local.MoWalkDatabase
import com.mowalk.app.data.sensor.StepDeltaCalculator
import com.mowalk.app.ui.main.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StepCounterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var stepDeltaCalculator: StepDeltaCalculator
    private lateinit var localDataSource: LocalDataSource
    private lateinit var sensorDataSource: com.mowalk.app.data.sensor.SensorDataSource
    private var stepCounterJob: Job? = null

    private val CHANNEL_ID = "step_channel"
    private val NOTIFICATION_ID = 1
    private val EXTRA_STOP = "extra_stop"

    override fun onCreate() {
        super.onCreate()
        stepDeltaCalculator = StepDeltaCalculator()
        val database = MoWalkDatabase.getDatabase(this)
        localDataSource = LocalDataSource(database.stepDao())
        sensorDataSource = com.mowalk.app.data.sensor.SensorDataSource(this)

        createNotificationChannel()
        loadYesterdayTotal()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras?.getBoolean(EXTRA_STOP) == true) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        stepCounterJob = serviceScope.launch {
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val yesterdayEntity = localDataSource.getByDate(yesterday)
            val yesterdayTotal = yesterdayEntity?.steps ?: 0
            stepDeltaCalculator.setYesterdayTotal(yesterdayTotal)
            registerSensorListener()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stepCounterJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun registerSensorListener() {
        if (sensorDataSource.isStepCounterAvailable) {
            sensorDataSource.observeSteps().onEach { cumulativeSteps ->
                val yesterdayTotal = stepDeltaCalculator.getYesterdayTotal()
                val todaySteps = stepDeltaCalculator.update(cumulativeSteps, yesterdayTotal)

                val userProfile = localDataSource.getUserProfile()
                val weight = userProfile?.weight ?: 70f
                val stepsPerKm = if (weight > 0) 12000f / weight else 15000f
                val distanceMeters = (todaySteps / stepsPerKm) * 1000f
                val calories = (todaySteps / 20f) * (weight / 70f)

                localDataSource.upsertTodaySteps(
                    steps = todaySteps,
                    distance = distanceMeters.toInt().toFloat(),
                    calories = calories
                )

                stepDeltaCalculator.setYesterdayTotal(todaySteps)
            }.launchIn(serviceScope)
        } else {
            // Sensor not available — log warning
            android.util.Log.w(TAG, "TYPE_STEP_COUNTER not available on this device")
        }
    }

    private fun loadYesterdayTotal() {
        serviceScope.launch {
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val yesterdayEntity = localDataSource.getByDate(yesterday)
            val yesterdayTotal = yesterdayEntity?.steps ?: 0
            stepDeltaCalculator.setYesterdayTotal(yesterdayTotal)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.step_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.step_channel_description)
            setShowBadge(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, StepCounterService::class.java).apply {
            putExtra(EXTRA_STOP, true)
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.step_notification_title))
            .setContentText(getString(R.string.step_notification_text))
            .setSmallIcon(R.drawable.ic_step_notification)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, getString(R.string.stop_service), stopPendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "StepCounterService"
    }
}
