package com.mowalk.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mowalk.app.data.local.UserProfileEntity
import com.mowalk.app.data.repository.StepRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val height: Float? = null,
    val weight: Float? = null,
    val dailyStepGoal: Int = 8000,
    val stepCounterAvailable: Boolean = true,
    val versionName: String = "1.0.0"
)

class SettingsViewModel(
    val repository: StepRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getUserProfile()?.let { profile ->
                    _state.value = _state.value.copy(
                        height = profile.height,
                        weight = profile.weight,
                        dailyStepGoal = profile.dailyStepGoal
                    )
                }
            } catch (e: Exception) {
                // Use default values
            }
        }
    }

    fun updateHeight(height: Float) {
        val profile = _state.value
        viewModelScope.launch {
            repository.updateUserProfile(
                height = height,
                weight = profile.weight,
                dailyStepGoal = profile.dailyStepGoal
            )
            _state.value = _state.value.copy(height = height)
        }
    }

    fun updateWeight(weight: Float) {
        val profile = _state.value
        viewModelScope.launch {
            repository.updateUserProfile(
                height = profile.height,
                weight = weight,
                dailyStepGoal = profile.dailyStepGoal
            )
            _state.value = _state.value.copy(weight = weight)
        }
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            repository.updateUserProfile(
                height = _state.value.height,
                weight = _state.value.weight,
                dailyStepGoal = goal
            )
            _state.value = _state.value.copy(dailyStepGoal = goal)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun setVersionName(name: String) {
        _state.value = _state.value.copy(versionName = name)
    }
}
