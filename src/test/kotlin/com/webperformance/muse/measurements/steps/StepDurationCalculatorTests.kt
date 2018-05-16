package com.webperformance.muse.measurements.steps

import org.junit.*

class StepDurationCalculatorTests
{
	val calculator = StepDurationCalculator()
	
	@Test
	fun missingStart()
	{
		val duration = calculator.getDuration("num1", System.nanoTime())
		Assert.assertNull(duration)
	}
	
	@Test
	fun duplicateStart()
	{
		calculator.recordStartTime("num1", System.nanoTime())
		val recorded = calculator.recordStartTime("num1", System.nanoTime())
		Assert.assertFalse(recorded)
	}
	
	@Test
	fun oneAndTwoDurationsDifferentIds()
	{
		val now = System.nanoTime()

		calculator.recordStartTime("num1", now)
		val duration = calculator.getDuration("num1", now + 1000L)
		Assert.assertEquals(1000L, duration)

		calculator.recordStartTime("num2", now + 1000L)
		val duration2 = calculator.getDuration("num2", now + 3000L)
		Assert.assertEquals(2000L, duration2)
	}

	@Test
	fun restartFinishedId()
	{
		val now = System.nanoTime()

		calculator.recordStartTime("num1", now)
		val duration = calculator.getDuration("num1", now + 1000L)
		Assert.assertEquals(1000L, duration)

		calculator.recordStartTime("num1", now + 1000L)
		val duration2 = calculator.getDuration("num1", now + 3000L)
		Assert.assertEquals(2000L, duration2)
	}
	
	@Test
	fun overlappingDurations()
	{
		val now = System.nanoTime()
		calculator.recordStartTime("num1", now)
		calculator.recordStartTime("num2", now + 3000L)

		val duration = calculator.getDuration("num1", now + 1000L)
		Assert.assertEquals(1000L, duration)

		val duration2 = calculator.getDuration("num2", now + 6000L)
		Assert.assertEquals(3000L, duration2)
	}
}