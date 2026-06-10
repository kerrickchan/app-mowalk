package com.mowalk.app.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorDataSource(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isStepCounterAvailable: Boolean get() = stepCounterSensor != null
    val isAccelerometerAvailable: Boolean get() = accelerometerSensor != null

    fun observeSteps(): Flow<Int> = callbackFlow {
        if (stepCounterSensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    trySend(it.values[0].toInt())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun observeAccelSteps(): Flow<Int> = callbackFlow {
        if (accelerometerSensor == null) {
            close()
            return@callbackFlow
        }

        var lastTime = 0L
        var lastAccel = 0f
        var stepCount = 0
        val STEP_THRESHOLD = 12f
        val MIN_STEP_INTERVAL = 300L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { e ->
                    val now = e.timestamp
                    if (now - lastTime < MIN_STEP_INTERVAL) return

                    val magnitude = Math.sqrt(
                        (e.values[0] * e.values[0] +
                                e.values[1] * e.values[1] +
                                e.values[2] * e.values[2]).toDouble()
                    ).toFloat()

                    if (magnitude > lastAccel + STEP_THRESHOLD) {
                        stepCount++
                        trySend(stepCount)
                    }

                    lastAccel = magnitude
                    lastTime = now
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
