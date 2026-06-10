package com.mowalk.app.export

import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.repository.StepRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CsvExporterTest {

    private val exporter = CsvExporter()

    @Test
    fun `formatCsv includes header row`() = runTest {
        val repository: StepRepository = mockk()
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val csv = exporter.formatCsv(repository)

        assertTrue(csv.startsWith("date,steps,distance,calories,isManuallyEdited"))
        assertTrue(csv.contains("\n"))
    }

    @Test
    fun `formatCsv formats entities correctly`() = runTest {
        val entities = listOf(
            DailyStepEntity(
                date = "2024-06-01",
                steps = 5000,
                distance = 2.5f,
                calories = 250.0f,
                isManuallyEdited = false
            )
        )
        val repository: StepRepository = mockk()
        every { repository.getRange(any(), any()) } returns flowOf(entities)

        val csv = exporter.formatCsv(repository)

        val expected = "date,steps,distance,calories,isManuallyEdited\n" +
            "2024-06-01,5000,2.5,250.0,0\n"
        assertEquals(expected, csv)
    }

    @Test
    fun `formatCsv maps isManuallyEdited to 1 for true`() = runTest {
        val entities = listOf(
            DailyStepEntity(
                date = "2024-06-01",
                steps = 1000,
                distance = 1.0f,
                calories = 50.0f,
                isManuallyEdited = true
            )
        )
        val repository: StepRepository = mockk()
        every { repository.getRange(any(), any()) } returns flowOf(entities)

        val csv = exporter.formatCsv(repository)

        assertTrue(csv.contains(",1\n"))
    }

    @Test
    fun `formatCsv handles empty entity list`() = runTest {
        val repository: StepRepository = mockk()
        every { repository.getRange(any(), any()) } returns flowOf(emptyList())

        val csv = exporter.formatCsv(repository)

        assertEquals("date,steps,distance,calories,isManuallyEdited\n", csv)
    }

    @Test
    fun `formatCsv sorts entities by date`() = runTest {
        val entities = listOf(
            DailyStepEntity(date = "2024-06-03", steps = 3000, distance = 1.5f, calories = 150.0f),
            DailyStepEntity(date = "2024-06-01", steps = 1000, distance = 0.5f, calories = 50.0f),
            DailyStepEntity(date = "2024-06-02", steps = 2000, distance = 1.0f, calories = 100.0f)
        )
        val repository: StepRepository = mockk()
        every { repository.getRange(any(), any()) } returns flowOf(entities)

        val csv = exporter.formatCsv(repository)

        val lines = csv.trim().lines()
        assertEquals("date,steps,distance,calories,isManuallyEdited", lines[0])
        assertTrue(lines[1].startsWith("2024-06-01"))
        assertTrue(lines[2].startsWith("2024-06-02"))
        assertTrue(lines[3].startsWith("2024-06-03"))
    }

    @Test
    fun `formatCsv includes all five columns per entity`() = runTest {
        val entities = listOf(
            DailyStepEntity(
                date = "2024-12-25",
                steps = 15000,
                distance = 7.5f,
                calories = 750.0f,
                isManuallyEdited = true
            )
        )
        val repository: StepRepository = mockk()
        every { repository.getRange(any(), any()) } returns flowOf(entities)

        val csv = exporter.formatCsv(repository)

        val expected = "date,steps,distance,calories,isManuallyEdited\n" +
            "2024-12-25,15000,7.5,750.0,1\n"
        assertEquals(expected, csv)
    }
}
