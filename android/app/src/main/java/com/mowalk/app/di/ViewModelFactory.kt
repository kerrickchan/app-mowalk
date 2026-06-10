package com.mowalk.app.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mowalk.app.data.local.LocalDataSource
import com.mowalk.app.data.local.MoWalkDatabase
import com.mowalk.app.data.repository.StepRepository
import com.mowalk.app.ui.dashboard.DashboardViewModel
import com.mowalk.app.ui.settings.SettingsViewModel
import com.mowalk.app.ui.trends.TrendsViewModel
import com.mowalk.app.ui.calendar.CalendarViewModel

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val database = MoWalkDatabase.getDatabase(application)
    private val localDataSource = LocalDataSource(database.stepDao())
    private val repository = StepRepository(localDataSource)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            DashboardViewModel::class.java -> DashboardViewModel(repository) as T
            SettingsViewModel::class.java -> SettingsViewModel(repository) as T
            TrendsViewModel::class.java -> TrendsViewModel(repository) as T
            CalendarViewModel::class.java -> CalendarViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
