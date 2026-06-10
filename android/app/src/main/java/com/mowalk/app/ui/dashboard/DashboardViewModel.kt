package com.mowalk.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.repository.StepRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardState(
    val todaySteps: Int = 0,
    val distance: Float = 0f,
    val calories: Float = 0f,
    val dailyGoal: Int = 8000,
    val progressPercent: Float = 0f,
    val isStepCounterAvailable: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeTodaySteps().collect { today ->
                    val steps = today?.steps ?: 0
                    val distance = today?.distance ?: 0f
                    val calories = today?.calories ?: 0f
                    val goal = _state.value.dailyGoal
                    val progress = if (goal > 0) steps.toFloat() / goal else 0f

                    _state.value = _state.value.copy(
                        todaySteps = steps,
                        distance = distance,
                        calories = calories,
                        progressPercent = progress,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Unknown error")
            }
        }

        viewModelScope.launch {
            try {
                repository.getUserProfile()?.let { profile ->
                    _state.value = _state.value.copy(
                        dailyGoal = profile.dailyStepGoal
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        // Steps are synced in real-time via sensor — no manual sync needed
    }

    fun updateError() {
        _state.value = _state.value.copy(error = null)
    }

    fun openSettings() {}
    fun openTrends() {}
    fun openCalendar() {}
}
