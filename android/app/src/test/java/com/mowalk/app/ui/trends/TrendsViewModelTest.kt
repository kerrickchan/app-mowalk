package com.mowalk.app.ui.trends

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModelTest {

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
    fun `initial state loads weekly data and defaults to WEEK period`() {
        val weeklyData = listOf(
            DailyStepEntity(date = "2024-06-10", steps = 3000),
            DailyStepEntity(date = "2024-06-11", steps = 5000)
        )
        every { repository.getWeeklyData() } returns flowOf(weeklyData)

        val viewModel = TrendsViewModel(repository)

        assertEquals(TrendsPeriod.WEEK, viewModel.state.value.period)
        assertEquals(2, viewModel.state.value.dataPoints.size)
        assertEquals("2024-06-10", viewModel.state.value.dataPoints[0].date)
    }

    @Test
    fun `initial state has no selected day`() {
        every { repository.getWeeklyData() } returns flowOf(emptyList())

        val viewModel = TrendsViewModel(repository)

        assertNull(viewModel.state.value.selectedDay)
    }

    @Test
    fun `setPeriod to WEEK reloads weekly data`() {
        every { repository.getWeeklyData() } returns flowOf(emptyList())
        every { repository.getMonthlyData(any(), any()) } returns flowOf(emptyList())

        val viewModel = TrendsViewModel(repository)
        // switch to month first to change state
        viewModel.setPeriod(TrendsPeriod.MONTH)
        // then back to week
        viewModel.setPeriod(TrendsPeriod.WEEK)

        assertEquals(TrendsPeriod.WEEK, viewModel.state.value.period)
    }

    @Test
    fun `setPeriod to MONTH reloads monthly data`() {
        val monthlyData = listOf(
            DailyStepEntity(date = "2024-06-01", steps = 4000),
            DailyStepEntity(date = "2024-06-02", steps = 6000)
        )
        every { repository.getWeeklyData() } returns flowOf(emptyList())
        every { repository.getMonthlyData(any(), any()) } returns flowOf(monthlyData)

        val viewModel = TrendsViewModel(repository)
        viewModel.setPeriod(TrendsPeriod.MONTH)

        assertEquals(TrendsPeriod.MONTH, viewModel.state.value.period)
        assertEquals(2, viewModel.state.value.dataPoints.size)
        assertEquals("2024-06-01", viewModel.state.value.dataPoints[0].date)
    }

    @Test
    fun `onSelectDay updates selected day`() {
        every { repository.getWeeklyData() } returns flowOf(emptyList())

        val viewModel = TrendsViewModel(repository)
        val day = DailyStepEntity(date = "2024-06-15", steps = 5000)
        viewModel.onSelectDay(day)

        assertEquals(day, viewModel.state.value.selectedDay)
        assertEquals(5000, viewModel.state.value.selectedDay?.steps)
    }

    @Test
    fun `onDismissDetail clears selected day`() {
        every { repository.getWeeklyData() } returns flowOf(emptyList())

        val viewModel = TrendsViewModel(repository)
        viewModel.onSelectDay(DailyStepEntity(date = "2024-06-15", steps = 5000))
        viewModel.onDismissDetail()

        assertNull(viewModel.state.value.selectedDay)
    }

    @Test
    fun `error during weekly load sets isLoading to false`() {
        every { repository.getWeeklyData() } throws RuntimeException("Network error")

        val viewModel = TrendsViewModel(repository)

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.dataPoints.isEmpty())
    }

    @Test
    fun `error during monthly load sets isLoading to false`() {
        every { repository.getWeeklyData() } returns flowOf(emptyList())
        every { repository.getMonthlyData(any(), any()) } throws RuntimeException("Network error")

        val viewModel = TrendsViewModel(repository)
        viewModel.setPeriod(TrendsPeriod.MONTH)

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.dataPoints.isEmpty())
    }
}
