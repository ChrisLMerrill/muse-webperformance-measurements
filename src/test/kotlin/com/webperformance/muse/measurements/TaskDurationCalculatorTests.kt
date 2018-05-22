package com.webperformance.muse.measurements

import org.junit.*

class TaskDurationCalculatorTests
{
	val calculator = TaskDurationCalculator()
	
	@Test
	fun missingStart()
	{
		val duration = calculator.getDuration("exe1", "step1", System.currentTimeMillis())
		Assert.assertNull(duration)
	}
	
	@Test
	fun duplicateStart()
	{
		calculator.recordStartTime("exe1", "step1", System.currentTimeMillis())
		val recorded = calculator.recordStartTime("exe1", "step1", System.currentTimeMillis())
		Assert.assertFalse(recorded)
	}
	
	@Test
	fun oneAndTwoDurationsDifferentIds()
	{
		val now = System.currentTimeMillis()

		calculator.recordStartTime("exe1", "step1", now)
		val duration = calculator.getDuration("exe1", "step1", now + 1000L)
		Assert.assertEquals(1000L, duration)

		calculator.recordStartTime("exe1", "step2", now + 1000L)
		val duration2 = calculator.getDuration("exe1", "step2", now + 3000L)
		Assert.assertEquals(2000L, duration2)
	}

	@Test
	fun restartFinishedId()
	{
		val now = System.currentTimeMillis()

		calculator.recordStartTime("exe1", "step1", now)
		val duration = calculator.getDuration("exe1", "step1", now + 1000L)
		Assert.assertEquals(1000L, duration)

		calculator.recordStartTime("exe1", "step1", now + 1000L)
		val duration2 = calculator.getDuration("exe1", "step1", now + 3000L)
		Assert.assertEquals(2000L, duration2)
	}
	
	@Test
	fun overlappingDurations()
	{
		val now = System.currentTimeMillis()
		calculator.recordStartTime("exe1", "step1", now)
		calculator.recordStartTime("exe1", "step2", now + 3000L)

		val duration = calculator.getDuration("exe1", "step1", now + 1000L)
		Assert.assertEquals(1000L, duration)

		val duration2 = calculator.getDuration("exe1", "step2", now + 6000L)
		Assert.assertEquals(3000L, duration2)
	}
	
	@Test
	fun zeroRunning()
	{
	    val counts = calculator.getRunningTaskCounts()
		Assert.assertTrue(counts.isEmpty())
		
		val durations = calculator.getRunningTaskDurations(System.currentTimeMillis())
		Assert.assertTrue(durations.isEmpty())
	}
	
	@Test
	fun oneRunningOneStep()
	{
		val now = System.currentTimeMillis()
		calculator.recordStartTime("123", "step1", now - 1000)

		val counts = calculator.getRunningTaskCounts()
		Assert.assertEquals(1, counts.size)

		val durations = calculator.getRunningTaskDurations(now)
		Assert.assertEquals(1000L, durations.values.iterator().next())
	}

	@Test
	fun twoRunningTwoSteps()
	{
		val now = System.currentTimeMillis()
		calculator.recordStartTime("123", "step1", now - 1000)
		calculator.recordStartTime("456", "step2", now - 2000)

		val counts = calculator.getRunningTaskCounts()
		Assert.assertEquals(2, counts.size)
		val iterator = counts.values.iterator()
		Assert.assertEquals(1, iterator.next())
		Assert.assertEquals(1, iterator.next())

		val durations = calculator.getRunningTaskDurations(now)
		Assert.assertEquals(1000L, durations["step1"])
		Assert.assertEquals(2000L, durations["step2"])
	}

	@Test
	fun twoRunningOneStep()
	{
		val now = System.currentTimeMillis()
		calculator.recordStartTime("123", "step1", now - 1000)
		calculator.recordStartTime("456", "step1", now - 2000)

		val counts = calculator.getRunningTaskCounts()
		Assert.assertEquals(1, counts.size)
		Assert.assertEquals(2, counts.values.iterator().next())

		val durations = calculator.getRunningTaskDurations(now)
		Assert.assertEquals(3000L, durations["step1"])
	}
}