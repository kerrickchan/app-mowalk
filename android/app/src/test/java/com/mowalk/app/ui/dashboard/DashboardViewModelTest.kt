package com.mowalk.app.ui.dashboard

import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.local.UserProfileEntity
import com.mowalk.app.data.repository.StepRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: StepRepository
    private lateinit var todayFlow: MutableStateFlow<DailyStepEntity?>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        todayFlow = MutableStateFlow(null)
        every { repository.observeTodaySteps() } returns todayFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = DashboardViewModel(repository)

        assertEquals(0, viewModel.state.value.todaySteps)
        assertEquals(0f, viewModel.state.value.distance)
        assertEquals(0f, viewModel.state.value.calories)
        assertEquals(8000, viewModel.state.value.dailyGoal)
        assertEquals(0f, viewModel.state.value.progressPercent)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isRefreshing)
        assertTrue(viewModel.state.value.isStepCounterAvailable)
    }

    @Test
    fun `today steps emission updates state`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = DashboardViewModel(repository)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 5000,
            distance = 2.5f,
            calories = 250.0f,
            isManuallyEdited = false
        )

        assertEquals(5000, viewModel.state.value.todaySteps)
        assertEquals(2.5f, viewModel.state.value.distance)
        assertEquals(250.0f, viewModel.state.value.calories)
        assertEquals(0.625f, viewModel.state.value.progressPercent)
    }

    @Test
    fun `null today entity results in zeros`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = DashboardViewModel(repository)

        // todayFlow already emits null by default
        assertEquals(0, viewModel.state.value.todaySteps)
        assertEquals(0f, viewModel.state.value.distance)
        assertEquals(0f, viewModel.state.value.calories)
    }

    @Test
    fun `user profile overrides daily goal`() {
        coEvery { repository.getUserProfile() } returns UserProfileEntity(
            dailyStepGoal = 12000
        )

        val viewModel = DashboardViewModel(repository)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 6000,
            distance = 3.0f,
            calories = 300.0f
        )

        assertEquals(12000, viewModel.state.value.dailyGoal)
        assertEquals(0.5f, viewModel.state.value.progressPercent)
    }

    @Test
    fun `progressPercent is zero when goal is zero`() {
        coEvery { repository.getUserProfile() } returns UserProfileEntity(
            dailyStepGoal = 0
        )

        val viewModel = DashboardViewModel(repository)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 5000,
            distance = 2.5f,
            calories = 250.0f
        )

        assertEquals(0f, viewModel.state.value.progressPercent)
    }

    @Test
    fun `progressPercent reaches one hundred percent when goal is met`() {
        coEvery { repository.getUserProfile() } returns UserProfileEntity(
            dailyStepGoal = 10000
        )

        val viewModel = DashboardViewModel(repository)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 10000,
            distance = 5.0f,
            calories = 500.0f
        )

        assertEquals(1.0f, viewModel.state.value.progressPercent)
    }

    @Test
    fun `progressPercent exceeds one when steps exceed goal`() {
        coEvery { repository.getUserProfile() } returns UserProfileEntity(
            dailyStepGoal = 5000
        )

        val viewModel = DashboardViewModel(repository)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 7500,
            distance = 3.75f,
            calories = 375.0f
        )

        assertEquals(1.5f, viewModel.state.value.progressPercent)
    }

    @Test
    fun `updateError clears error state`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = DashboardViewModel(repository)

        viewModel.updateError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `multiple step emissions update state in sequence`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = DashboardViewModel(repository)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 1000,
            distance = 0.5f,
            calories = 50.0f
        )

        assertEquals(1000, viewModel.state.value.todaySteps)

        todayFlow.value = DailyStepEntity(
            date = "2024-06-01",
            steps = 8000,
            distance = 4.0f,
            calories = 400.0f
        )

        assertEquals(8000, viewModel.state.value.todaySteps)
        assertEquals(4.0f, viewModel.state.value.distance)
        assertEquals(400.0f, viewModel.state.value.calories)
    }
}
