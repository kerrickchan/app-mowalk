package com.mowalk.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val height: Float? = null,
    val weight: Float? = null,
    val dailyStepGoal: Int = 8000
) {
    companion object {
        fun default(): UserProfileEntity = UserProfileEntity()
    }
}
