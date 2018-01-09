package com.webperformance.muse.measurements.stepduration

import org.junit.Assert
import org.junit.Test

class AverageStepDurationsCalculatorTests
{
	private val calculator = AverageStepDurationsCalculator()

	@Test
	fun averageOfOne()
	{
		val durations = StepDurations()
		durations.record(1, 100)
		calculator.add(durations)
		
		val averages = calculator.getAverages()
		
		Assert.assertEquals(1, averages.getStepIds().size)
		Assert.assertEquals(100, averages.getDurations(1)[0])
	}
	
	@Test
	fun averageOfTwo()
	{
		var durations = StepDurations()
		durations.record(1, 100)
		
		calculator.add(durations)

		durations = StepDurations()
		durations.record(1, 200)
		calculator.add(durations)
		
		val averages = calculator.getAverages()
		
		Assert.assertEquals(1, averages.getStepIds().size)
		Assert.assertEquals(150, averages.getDurations(1)[0])
	}
	
	@Test
	fun multiplesInList()
	{
		val durations = StepDurations()
		durations.record(1, 100)
		durations.record(1, 200)
		
		calculator.add(durations)
		
		val averages = calculator.getAverages()
		
		Assert.assertEquals(1, averages.getStepIds().size)
		Assert.assertEquals(150, averages.getDurations(1)[0])
	}
	
	@Test
	fun multipleSteps()
	{
		var durations = StepDurations()
		durations.record(1, 100)
		durations.record(2, 200)
		calculator.add(durations)

		durations = StepDurations()
		durations.record(1, 200)
		durations.record(2, 400)
		calculator.add(durations)

		val averages = calculator.getAverages()
		
		Assert.assertEquals(2, averages.getStepIds().size)
		Assert.assertEquals(150, averages.getDurations(1)[0])
		Assert.assertEquals(300, averages.getDurations(2)[0])
	}
}