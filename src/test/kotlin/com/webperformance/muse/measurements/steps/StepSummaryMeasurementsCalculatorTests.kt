package com.webperformance.muse.measurements.steps

import org.junit.*

class StepSummaryMeasurementsCalculatorTests
{
	private val calculator = StepSummaryMeasurementsCalculator()
	
	@Test
	fun oneSuccessStep()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		val counts = all_counts[0]
		Assert.assertEquals(1, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun oneFailedStep()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", true, false)
		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		val counts = all_counts[0]
		Assert.assertEquals(0, counts.successes)
		Assert.assertEquals(1, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun oneErredStep()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, true)
		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		val counts = all_counts[0]
		Assert.assertEquals(0, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(1, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun oneStepInProgress()
	{
		calculator.startStep(123L, "test1")
		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		val counts = all_counts[0]
		Assert.assertEquals(0, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals(1, counts.running)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun oneStepRepeated()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		val counts = all_counts[0]
		Assert.assertEquals(2, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun oneStepTwoSamples()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		var all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		var counts = all_counts[0]
		Assert.assertEquals(1, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)

		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		all_counts = calculator.extractAllCounts()
		Assert.assertEquals(1, all_counts.size)
		counts = all_counts[0]
		Assert.assertEquals(1, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun twoSteps()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, true)
		calculator.startStep(456L, "test2")
		calculator.endStep(456L, "test2", true, false)

		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(2, all_counts.size)
		
		var counts = getCountsForStep(all_counts, 123L)
		Assert.assertNotNull(counts)
		Assert.assertEquals(1, counts!!.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)

		counts = getCountsForStep(all_counts, 456L)
		Assert.assertNotNull(counts)
		Assert.assertEquals(1, counts!!.failures)
		Assert.assertEquals("test2", counts.test_id)
		Assert.assertEquals(456L, counts.step_id)
	}
	
	private fun getCountsForStep(counts: List<StepSummaryMeasurementsCalculator.Counts>, stepid: Long): StepSummaryMeasurementsCalculator.Counts?
	{
		for (counter in counts)
			if (counter.step_id == stepid)
				return counter
		return null
	}
	
	@Test
	fun oneStepTwoTests()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		calculator.startStep(123L, "test2")
		calculator.endStep(123L, "test2", false, false)

		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(2, all_counts.size)
		var counts = all_counts[0]
		Assert.assertEquals(1, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)

		counts = all_counts[1]
		Assert.assertEquals(1, counts.successes)
		Assert.assertEquals(0, counts.failures)
		Assert.assertEquals(0, counts.errors)
		Assert.assertEquals("test2", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)
	}
	
	@Test
	fun twoStepsTwoTestsRepeated()
	{
		calculator.startStep(123L, "test1")
		calculator.endStep(123L, "test1", false, false)
		calculator.startStep(123L, "test2")
		calculator.endStep(123L, "test2", true, false)

		calculator.startStep(456L, "test1")
		calculator.endStep(456L, "test1", false, true)
		calculator.startStep(456L, "test2")
		
		val all_counts = calculator.extractAllCounts()
		Assert.assertEquals(4, all_counts.size)
		
		var counts = getCountsForStepAndTest(all_counts, 123L, "test1")
		Assert.assertNotNull(counts)
		Assert.assertEquals(1, counts!!.successes)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)

		counts = getCountsForStepAndTest(all_counts, 123L, "test2")
		Assert.assertNotNull(counts)
		Assert.assertEquals(1, counts!!.failures)
		Assert.assertEquals("test2", counts.test_id)
		Assert.assertEquals(123L, counts.step_id)

		counts = getCountsForStepAndTest(all_counts, 456L, "test1")
		Assert.assertNotNull(counts)
		Assert.assertEquals(1, counts!!.errors)
		Assert.assertEquals("test1", counts.test_id)
		Assert.assertEquals(456L, counts.step_id)

		counts = getCountsForStepAndTest(all_counts, 456L, "test2")
		Assert.assertNotNull(counts)
		Assert.assertEquals(0, counts!!.successes)
		Assert.assertEquals(1, counts.running)
		Assert.assertEquals("test2", counts.test_id)
		Assert.assertEquals(456L, counts.step_id)
	}
	
	private fun getCountsForStepAndTest(counts: List<StepSummaryMeasurementsCalculator.Counts>, stepid: Long, testid: String): StepSummaryMeasurementsCalculator.Counts?
	{
		for (counter in counts)
			if (counter.step_id == stepid && counter.test_id == testid)
				return counter
		return null
	}
	
}