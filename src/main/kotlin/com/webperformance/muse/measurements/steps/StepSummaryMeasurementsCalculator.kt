package com.webperformance.muse.measurements.steps

class StepSummaryMeasurementsCalculator
{
	private val calculators = mutableMapOf<String, StepCalculator>()
	
	fun startStep(step_id: Long, test_id: String)
	{
		getCalculator(step_id, test_id).running++
	}

	fun endStep(step_id: Long, test_id: String, failure: Boolean, error: Boolean)
	{
		val calculator = getCalculator(step_id, test_id)
		calculator.running--
		if (failure)
			calculator.failures++
		else if (error)
			calculator.errors++
		else
			calculator.successes++
	}
	
	private fun getCalculator(step_id: Long, test_id: String) : StepCalculator
	{
		val id = step_id.toString() + test_id
		var calc = calculators[id]
		if (calc == null)
		{
			calc = StepCalculator(step_id, test_id)
			calculators[id] = calc
		}
		return calc
	}
	
	fun extractAllCounts(): List<Counts>
	{
		val counts = mutableListOf<Counts>()
		for (calc in calculators.values)
			counts.add(calc.extractCounts())
		return counts
	}
	
	private inner class StepCalculator(val step_id: Long, val test_id: String)
	{
		var successes = 0
		var failures = 0
		var errors = 0
		var running = 0
		
		fun extractCounts(): Counts
		{
			val counts = Counts(step_id, test_id, successes, failures, errors, running)
			successes = 0
			failures = 0
			errors = 0
			return counts
		}
	}
	
	inner class Counts(val step_id: Long, val test_id: String, val successes: Int, val failures: Int, val errors: Int, val running: Int)
}