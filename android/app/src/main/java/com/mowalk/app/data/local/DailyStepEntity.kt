package com.mowalk.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String,
    val steps: Int = 0,
    val distance: Float = 0f,
    val calories: Float = 0f,
    val isManuallyEdited: Boolean = false
) {
    companion object {
        fun today(): String = java.time.LocalDate.now().toString()
    }
}
