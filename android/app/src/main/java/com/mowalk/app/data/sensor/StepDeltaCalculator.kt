package com.mowalk.app.data.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepDeltaCalculator {

    private val _bootOffset = MutableStateFlow<Int?>(null)
    private val _previousDayTotal = MutableStateFlow(0)
    private val _lastKnownToday = MutableStateFlow(0)

    fun setYesterdayTotal(total: Int) {
        _previousDayTotal.value = total
    }

    fun update(cumulativeSteps: Int, yesterdayTotal: Int): Int {
        _previousDayTotal.value = yesterdayTotal

        val bootOffset = _bootOffset.value
        if (bootOffset == null) {
            _bootOffset.value = cumulativeSteps
            return 0
        }

        val rawToday = cumulativeSteps - bootOffset - yesterdayTotal
        val todaySteps = maxOf(rawToday, _lastKnownToday.value)
        _lastKnownToday.value = todaySteps
        return todaySteps
    }

    fun onDeviceReboot(cumulativeSteps: Int, yesterdayTotal: Int): Int {
        _bootOffset.value = cumulativeSteps
        _previousDayTotal.value = yesterdayTotal
        _lastKnownToday.value = 0
        return 0
    }

    fun resetForNewDay(yesterdayTotal: Int) {
        _bootOffset.value = null
        _previousDayTotal.value = yesterdayTotal
        _lastKnownToday.value = 0
    }

    fun getLastKnownToday(): Int = _lastKnownToday.value
    fun getYesterdayTotal(): Int = _previousDayTotal.value
}
