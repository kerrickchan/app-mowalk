package com.mowalk.app.ui.calendar

import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.repository.StepRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: StepRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads current month days`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)

        assertTrue(viewModel.state.value.monthDays.isNotEmpty())
        assertTrue(viewModel.state.value.monthDays.any { it.isCurrentMonth })
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `month grid contains 42 calendar cells`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)

        assertEquals(42, viewModel.state.value.monthDays.size)
    }

    @Test
    fun `step data populates month day steps`() {
        val today = DailyStepEntity.today()
        every { repository.getRange(any(), any()) } returns flowOf(
            listOf(DailyStepEntity(date = today, steps = 5000))
        )

        val viewModel = CalendarViewModel(repository)

        val todayDay = viewModel.state.value.monthDays.find { it.isToday }
        assertNotNull(todayDay)
        assertEquals(5000, todayDay!!.steps)
    }

    @Test
    fun `onSelectDay updates selected date`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)
        viewModel.onSelectDay("2024-06-15")

        assertEquals("2024-06-15", viewModel.state.value.selectedDate)
    }

    @Test
    fun `getStepCountForDate returns step count from loaded data`() {
        every { repository.getRange(any(), any()) } returns flowOf(
            listOf(DailyStepEntity(date = "2024-06-15", steps = 7500))
        )

        val viewModel = CalendarViewModel(repository)

        assertEquals(7500, viewModel.getStepCountForDate("2024-06-15"))
    }

    @Test
    fun `getStepCountForDate returns null for unknown date`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)

        assertNull(viewModel.getStepCountForDate("2024-06-15"))
    }

    @Test
    fun `swipeToPreviousMonth updates current month`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)
        val initialMonth = viewModel.state.value.currentMonth
        viewModel.swipeToPreviousMonth()

        val newMonth = viewModel.state.value.currentMonth
        // previous month should be different from initial
        assertTrue(newMonth.year < initialMonth.year || newMonth.month < initialMonth.month)
    }

    @Test
    fun `swipeToNextMonth updates current month`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)
        val initialMonth = viewModel.state.value.currentMonth
        viewModel.swipeToNextMonth()

        val newMonth = viewModel.state.value.currentMonth
        // next month should be different from initial
        assertTrue(newMonth.year > initialMonth.year || newMonth.month > initialMonth.month)
    }

    @Test
    fun `YearMonth plusMonths wraps year correctly`() {
        val december = YearMonth(year = 2024, month = 12)
        val january2025 = december.plusMonths(1)

        assertEquals(2025, january2025.year)
        assertEquals(1, january2025.month)
    }

    @Test
    fun `YearMonth minusMonths wraps year correctly`() {
        val january = YearMonth(year = 2024, month = 1)
        val december2023 = january.minusMonths(1)

        assertEquals(2023, december2023.year)
        assertEquals(12, december2023.month)
    }

    @Test
    fun `YearMonth first and last day are correct`() {
        val feb2024 = YearMonth(year = 2024, month = 2)
        val first = feb2024.getFirstDayOfMonth()
        val last = feb2024.getLastDayOfMonth()

        assertEquals(2024, first.year)
        assertEquals(2, first.monthNumber)
        assertEquals(1, first.dayOfMonth)
        assertEquals(2024, last.year)
        assertEquals(2, last.monthNumber)
        assertEquals(29, last.dayOfMonth)
    }

    @Test
    fun `error during load sets isLoading to false`() {
        every { repository.getRange(any(), any()) } throws RuntimeException("Database error")

        val viewModel = CalendarViewModel(repository)

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initially no day is selected`() {
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(repository)

        assertNull(viewModel.state.value.selectedDate)
    }
}
