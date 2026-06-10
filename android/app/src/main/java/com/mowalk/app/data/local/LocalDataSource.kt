package com.mowalk.app.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LocalDataSource(private val stepDao: StepDao) {

    fun observeTodaySteps(): Flow<DailyStepEntity?> =
        stepDao.observeByDate(DailyStepEntity.today())

    fun observeByDate(date: String): Flow<DailyStepEntity?> =
        stepDao.observeByDate(date)

    fun observeRange(start: String, end: String): Flow<List<DailyStepEntity>> =
        stepDao.observeRange(start, end)

    suspend fun getByDate(date: String): DailyStepEntity? =
        stepDao.getByDate(date)

    suspend fun sumSteps(start: String, end: String): Int? =
        stepDao.sumSteps(start, end)

    suspend fun upsertTodaySteps(steps: Int, distance: Float, calories: Float, isManuallyEdited: Boolean = false) {
        stepDao.insert(
            DailyStepEntity(
                date = DailyStepEntity.today(),
                steps = steps,
                distance = distance,
                calories = calories,
                isManuallyEdited = isManuallyEdited
            )
        )
    }

    suspend fun updateManualEntry(date: String, steps: Int, distance: Float, calories: Float) {
        stepDao.insert(
            DailyStepEntity(
                date = date,
                steps = steps,
                distance = distance,
                calories = calories,
                isManuallyEdited = true
            )
        )
    }

    suspend fun getUserProfile(): UserProfileEntity? =
        stepDao.getUserProfile()

    fun observeUserProfile(): Flow<UserProfileEntity?> =
        stepDao.observeUserProfile()

    suspend fun updateUserProfile(height: Float?, weight: Float?, dailyStepGoal: Int) {
        stepDao.upsertProfile(
            UserProfileEntity(
                id = 1,
                height = height,
                weight = weight,
                dailyStepGoal = dailyStepGoal
            )
        )
    }

    suspend fun clearAllData() {
        stepDao.deleteAll()
        stepDao.upsertProfile(UserProfileEntity.default())
    }

    suspend fun getStepsInRange(start: String, end: String): List<DailyStepEntity> {
        return stepDao.observeRange(start, end).first()
    }
}
