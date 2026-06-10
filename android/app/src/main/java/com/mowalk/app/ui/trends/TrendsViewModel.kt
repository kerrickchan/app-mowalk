package com.mowalk.app.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.repository.StepRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TrendsState(
    val dataPoints: List<DailyStepEntity> = emptyList(),
    val period: TrendsPeriod = TrendsPeriod.WEEK,
    val isLoading: Boolean = false,
    val selectedDay: DailyStepEntity? = null
)

enum class TrendsPeriod {
    WEEK, MONTH
}

class TrendsViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrendsState())
    val state: StateFlow<TrendsState> = _state.asStateFlow()

    init {
        loadWeeklyData()
    }

    fun setPeriod(period: TrendsPeriod) {
        _state.value = _state.value.copy(period = period)
        when (period) {
            TrendsPeriod.WEEK -> loadWeeklyData()
            TrendsPeriod.MONTH -> loadMonthlyData()
        }
    }

    fun onSelectDay(day: DailyStepEntity) {
        _state.value = _state.value.copy(selectedDay = day)
    }

    fun onDismissDetail() {
        _state.value = _state.value.copy(selectedDay = null)
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            try {
                repository.getWeeklyData().collect { data ->
                    _state.value = _state.value.copy(dataPoints = data)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun loadMonthlyData() {
        val now = java.time.LocalDate.now()
        viewModelScope.launch {
            try {
                repository.getMonthlyData(now.year, now.monthValue).collect { data ->
                    _state.value = _state.value.copy(dataPoints = data)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
