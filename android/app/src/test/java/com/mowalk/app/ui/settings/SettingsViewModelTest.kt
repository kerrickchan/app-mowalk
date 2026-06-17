package com.mowalk.app.ui.settings

import com.mowalk.app.data.local.UserProfileEntity
import com.mowalk.app.data.repository.StepRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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
    fun `initial state has default values`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = SettingsViewModel(repository)

        assertNull(viewModel.state.value.height)
        assertNull(viewModel.state.value.weight)
        assertEquals(8000, viewModel.state.value.dailyStepGoal)
        assertTrue(viewModel.state.value.stepCounterAvailable)
        assertEquals("1.0.0", viewModel.state.value.versionName)
    }

    @Test
    fun `init loads user profile from repository`() {
        coEvery { repository.getUserProfile() } returns UserProfileEntity(
            height = 170f,
            weight = 65f,
            dailyStepGoal = 10000
        )

        val viewModel = SettingsViewModel(repository)

        assertEquals(170f, viewModel.state.value.height)
        assertEquals(65f, viewModel.state.value.weight)
        assertEquals(10000, viewModel.state.value.dailyStepGoal)
    }

    @Test
    fun `init catches repository error and falls back to defaults`() {
        coEvery { repository.getUserProfile() } throws RuntimeException("Database error")

        val viewModel = SettingsViewModel(repository)

        assertNull(viewModel.state.value.height)
        assertNull(viewModel.state.value.weight)
        assertEquals(8000, viewModel.state.value.dailyStepGoal)
    }

    @Test
    fun `updateHeight calls repository and updates state`() {
        coEvery { repository.getUserProfile() } returns null
        coEvery { repository.updateUserProfile(any(), any(), any()) } just runs

        val viewModel = SettingsViewModel(repository)
        viewModel.updateHeight(175f)

        coVerify { repository.updateUserProfile(height = 175f, weight = null, dailyStepGoal = 8000) }
        assertEquals(175f, viewModel.state.value.height)
    }

    @Test
    fun `updateWeight calls repository and updates state`() {
        coEvery { repository.getUserProfile() } returns null
        coEvery { repository.updateUserProfile(any(), any(), any()) } just runs

        val viewModel = SettingsViewModel(repository)
        viewModel.updateWeight(72f)

        coVerify { repository.updateUserProfile(height = null, weight = 72f, dailyStepGoal = 8000) }
        assertEquals(72f, viewModel.state.value.weight)
    }

    @Test
    fun `updateDailyGoal calls repository and updates state`() {
        coEvery { repository.getUserProfile() } returns null
        coEvery { repository.updateUserProfile(any(), any(), any()) } just runs

        val viewModel = SettingsViewModel(repository)
        viewModel.updateDailyGoal(12000)

        coVerify { repository.updateUserProfile(height = null, weight = null, dailyStepGoal = 12000) }
        assertEquals(12000, viewModel.state.value.dailyStepGoal)
    }

    @Test
    fun `clearAllData calls repository`() {
        coEvery { repository.getUserProfile() } returns null
        coEvery { repository.clearAllData() } just runs

        val viewModel = SettingsViewModel(repository)
        viewModel.clearAllData()

        coVerify { repository.clearAllData() }
    }

    @Test
    fun `setVersionName updates state`() {
        coEvery { repository.getUserProfile() } returns null

        val viewModel = SettingsViewModel(repository)
        viewModel.setVersionName("2.0.0")

        assertEquals("2.0.0", viewModel.state.value.versionName)
    }
}
