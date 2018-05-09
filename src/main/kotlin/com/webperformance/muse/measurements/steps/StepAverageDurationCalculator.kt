package com.webperformance.muse.measurements.steps

import org.slf4j.*
import java.util.HashMap

class StepAverageDurationCalculator
{
	val start_times = HashMap<String, Long>()
	var total = 0L
	var count = 0L
	
	fun recordStartTime(id: String, time: Long)
	{
		if (start_times.containsKey(id))
			LOG.error("start-time already recorded for step $id. Possibly the previous iteration never ended? Is this step called recursively (not supported at this time)? Overwriting the previous value.")
		start_times.put(id, time)
	}

	fun recordFinish(id: String, time: Long)
	{
		val started: Long? = start_times.remove(id)
		if (started == null)
		{
			LOG.error(String.format("End event received for step %s but no start-time was found. Ignoring.", id))
			return
		}
		
		val duration = (time - started)/1000000
		total += duration
		count ++
	}
	
	fun calculateAndReset() : Result
	{
		val result : Result
		if (count == 0L)
			result = Result(null, 0)
		else
		{
			result = Result(total / count, count)
			total = 0L
			count = 0L
		}
		return result
	}
	
	data class Result(val average: Long?, val count: Long)
	
	companion object
	{
		val LOG = LoggerFactory.getLogger(StepAverageDurationCalculator::class.java)
	}
	
}