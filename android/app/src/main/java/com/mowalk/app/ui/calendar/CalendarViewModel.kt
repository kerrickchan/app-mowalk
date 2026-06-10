package com.mowalk.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.repository.StepRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

data class CalendarState(
    val monthDays: List<CalendarDay> = emptyList(),
    val selectedDate: String? = null,
    val isLoading: Boolean = false,
    val currentMonth: YearMonth = YearMonth.today()
)

data class CalendarDay(
    val date: String,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val steps: Int? = null
)

data class YearMonth(
    val year: Int,
    val month: Int
) {
    companion object {
        fun today(): YearMonth {
            val now = java.time.LocalDate.now()
            return YearMonth(now.year, now.monthValue)
        }
    }

    fun plusMonths(n: Int): YearMonth {
        val ld = java.time.LocalDate.of(year, month, 1).plusMonths(n.toLong())
        return YearMonth(ld.year, ld.monthValue)
    }

    fun minusMonths(n: Int): YearMonth {
        val ld = java.time.LocalDate.of(year, month, 1).minusMonths(n.toLong())
        return YearMonth(ld.year, ld.monthValue)
    }

    fun getFirstDayOfMonth(): LocalDate {
        return LocalDate(year, month, 1)
    }

    fun getLastDayOfMonth(): LocalDate {
        val ld = java.time.LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
        return LocalDate(ld.year, ld.monthValue, ld.dayOfMonth)
    }

    fun toStringFormatted(): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${monthNames[month - 1]} $year"
    }
}

class CalendarViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    private val _stepData = MutableStateFlow<Map<String, Int>>(emptyMap())

    init {
        loadMonthData(_state.value.currentMonth)
    }

    fun swipeToPreviousMonth() {
        val newMonth = _state.value.currentMonth.minusMonths(1)
        loadMonthData(newMonth)
    }

    fun swipeToNextMonth() {
        val newMonth = _state.value.currentMonth.plusMonths(1)
        loadMonthData(newMonth)
    }

    fun onSelectDay(date: String) {
        _state.value = _state.value.copy(selectedDate = date)
    }

    fun getStepCountForDate(date: String): Int? {
        return _stepData.value[date]
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                val start = yearMonth.getFirstDayOfMonth()
                val end = yearMonth.getLastDayOfMonth()

                repository.getRange(start.toString(), end.toString()).collect { data ->
                    val stepsMap = data.associate { it.date to it.steps }
                    _stepData.value = stepsMap

                    val days = generateMonthDays(yearMonth, stepsMap)
                    _state.value = _state.value.copy(
                        monthDays = days,
                        currentMonth = yearMonth
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun generateMonthDays(yearMonth: YearMonth, stepsMap: Map<String, Int>): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val firstDay = yearMonth.getFirstDayOfMonth()
        val lastDay = yearMonth.getLastDayOfMonth()
        val today = DailyStepEntity.today()

        val startDayOfWeek = firstDay.dayOfWeek.ordinal
        for (i in 0 until startDayOfWeek) {
            val prevDay = firstDay.minus((startDayOfWeek - i), kotlinx.datetime.DateTimeUnit.DAY)
            days.add(
                CalendarDay(
                    date = prevDay.toString(),
                    dayOfMonth = prevDay.dayOfMonth,
                    isCurrentMonth = false,
                    isToday = prevDay.toString() == today,
                    steps = stepsMap[prevDay.toString()]
                )
            )
        }

        var current = firstDay
        while (current <= lastDay) {
            days.add(
                CalendarDay(
                    date = current.toString(),
                    dayOfMonth = current.dayOfMonth,
                    isCurrentMonth = true,
                    isToday = current.toString() == today,
                    steps = stepsMap[current.toString()]
                )
            )
            current = current.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        }

        val remaining = 42 - days.size
        var nextMonthDay = lastDay.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        for (i in 0 until remaining) {
            days.add(
                CalendarDay(
                    date = nextMonthDay.toString(),
                    dayOfMonth = nextMonthDay.dayOfMonth,
                    isCurrentMonth = false,
                    isToday = nextMonthDay.toString() == today,
                    steps = stepsMap[nextMonthDay.toString()]
                )
            )
            nextMonthDay = nextMonthDay.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        }

        return days
    }
}
