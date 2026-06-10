package com.mowalk.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyStepEntity?

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyStepEntity?>

    @Query("SELECT * FROM daily_steps WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeRange(start: String, end: String): Flow<List<DailyStepEntity>>

    @Query("SELECT SUM(steps) FROM daily_steps WHERE date BETWEEN :start AND :end")
    suspend fun sumSteps(start: String, end: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyStepEntity)

    @Query("DELETE FROM daily_steps WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM daily_steps")
    suspend fun deleteAll()

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)
}
