package com.mowalk.app.data.sensor

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StepDeltaCalculatorTest {

    private lateinit var calculator: StepDeltaCalculator

    @Before
    fun setup() {
        calculator = StepDeltaCalculator()
    }

    @Test
    fun `first update captures boot offset and returns zero`() {
        val result = calculator.update(cumulativeSteps = 1000, yesterdayTotal = 500)
        assertEquals(0, result)
        assertEquals(0, calculator.getLastKnownToday())
    }

    @Test
    fun `second update computes step delta correctly`() {
        calculator.update(cumulativeSteps = 1000, yesterdayTotal = 0)
        val result = calculator.update(cumulativeSteps = 2800, yesterdayTotal = 300)
        assertEquals(1500, result)
    }

    @Test
    fun `steps never decrease due to maxOf with last known`() {
        calculator.update(cumulativeSteps = 100, yesterdayTotal = 0)
        val step1 = calculator.update(cumulativeSteps = 1100, yesterdayTotal = 0)
        assertEquals(1000, step1)

        val step2 = calculator.update(cumulativeSteps = 1100, yesterdayTotal = 500)
        assertEquals(1000, step2)
    }

    @Test
    fun `setYesterdayTotal updates stored value`() {
        calculator.setYesterdayTotal(4200)
        assertEquals(4200, calculator.getYesterdayTotal())
    }

    @Test
    fun `resetForNewDay clears boot offset and last known`() {
        calculator.update(cumulativeSteps = 1000, yesterdayTotal = 0)
        calculator.update(cumulativeSteps = 3000, yesterdayTotal = 0)
        assertEquals(2000, calculator.getLastKnownToday())

        calculator.resetForNewDay(yesterdayTotal = 2000)

        assertEquals(0, calculator.getLastKnownToday())
        assertEquals(2000, calculator.getYesterdayTotal())
    }

    @Test
    fun `after resetForNewDay first update recaptures boot offset`() {
        calculator.update(cumulativeSteps = 1000, yesterdayTotal = 0)
        calculator.update(cumulativeSteps = 3000, yesterdayTotal = 0)
        assertEquals(2000, calculator.getLastKnownToday())

        calculator.resetForNewDay(yesterdayTotal = 2000)

        val result = calculator.update(cumulativeSteps = 3500, yesterdayTotal = 2000)
        assertEquals(0, result)
    }

    @Test
    fun `onDeviceReboot resets boot offset and last known to zero`() {
        calculator.update(cumulativeSteps = 5000, yesterdayTotal = 0)
        calculator.update(cumulativeSteps = 8000, yesterdayTotal = 0)
        assertEquals(3000, calculator.getLastKnownToday())

        val result = calculator.onDeviceReboot(cumulativeSteps = 100, yesterdayTotal = 3000)

        assertEquals(0, result)
        assertEquals(0, calculator.getLastKnownToday())
        assertEquals(3000, calculator.getYesterdayTotal())
    }

    @Test
    fun `after onDeviceReboot next update recaptures boot offset`() {
        calculator.update(cumulativeSteps = 5000, yesterdayTotal = 0)
        calculator.update(cumulativeSteps = 8000, yesterdayTotal = 0)

        calculator.onDeviceReboot(cumulativeSteps = 100, yesterdayTotal = 3000)

        val nextUpdate = calculator.update(cumulativeSteps = 500, yesterdayTotal = 3000)
        assertEquals(0, nextUpdate)
    }

    @Test
    fun `getLastKnownToday returns zero initially`() {
        assertEquals(0, calculator.getLastKnownToday())
    }

    @Test
    fun `getYesterdayTotal returns zero initially`() {
        assertEquals(0, calculator.getYesterdayTotal())
    }

    @Test
    fun `update uses yesterdayTotal parameter not stored value`() {
        calculator.setYesterdayTotal(999)
        calculator.update(cumulativeSteps = 2000, yesterdayTotal = 100)

        val result = calculator.update(cumulativeSteps = 4000, yesterdayTotal = 100)
        assertEquals(1900, result)
    }
}
