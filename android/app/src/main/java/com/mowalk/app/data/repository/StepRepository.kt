package com.mowalk.app.data.repository

import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.local.LocalDataSource
import kotlinx.coroutines.flow.Flow

class StepRepository(
    private val localDataSource: LocalDataSource
) {

    val isStepCounterAvailable: Boolean = true

    fun observeTodaySteps(): Flow<DailyStepEntity?> {
        return localDataSource.observeTodaySteps()
    }

    fun observeByDate(date: String): Flow<DailyStepEntity?> {
        return localDataSource.observeByDate(date)
    }

    fun observeStepsRange(start: String, end: String): Flow<List<DailyStepEntity>> {
        return localDataSource.observeRange(start, end)
    }

    fun getWeeklyData(): Flow<List<DailyStepEntity>> {
        val today = java.time.LocalDate.now().toString()
        val start = java.time.LocalDate.now().minusDays(6).toString()
        return localDataSource.observeRange(start, today)
    }

    fun getMonthlyData(year: Int, month: Int): Flow<List<DailyStepEntity>> {
        val start = java.time.LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        return localDataSource.observeRange(start.toString(), end.toString())
    }

    suspend fun upsertTodaySteps(steps: Int, distance: Float, calories: Float) {
        localDataSource.upsertTodaySteps(steps, distance, calories)
    }

    suspend fun updateManualEntry(date: String, steps: Int, distance: Float, calories: Float) {
        localDataSource.updateManualEntry(date, steps, distance, calories)
    }

    suspend fun getTodaySteps(): DailyStepEntity? {
        return localDataSource.getByDate(DailyStepEntity.today())
    }

    suspend fun getUserProfile() = localDataSource.getUserProfile()

    suspend fun updateUserProfile(height: Float?, weight: Float?, dailyStepGoal: Int) {
        localDataSource.updateUserProfile(height, weight, dailyStepGoal)
    }

    suspend fun clearAllData() {
        localDataSource.clearAllData()
    }

    fun getStepsForDate(date: String): Flow<DailyStepEntity?> {
        return localDataSource.observeByDate(date)
    }

    fun getRange(start: String, end: String): Flow<List<DailyStepEntity>> {
        return localDataSource.observeRange(start, end)
    }
}
