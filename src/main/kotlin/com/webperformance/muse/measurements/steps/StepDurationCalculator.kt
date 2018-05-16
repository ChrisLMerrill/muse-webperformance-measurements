package com.webperformance.muse.measurements.steps

import org.slf4j.*
import java.util.*

/*
 * Tracks the start-times of steps and returns the duration when it completes.
 */
class StepDurationCalculator
{
	val start_times = HashMap<String, Long>()
	
	fun recordStartTime(id: String, time: Long): Boolean
	{
		if (start_times.containsKey(id))
		{
			LOG.error("start-time already recorded for step $id. Possibly the previous iteration never ended? Is this step called recursively (not supported at this time)? Overwriting the previous value.")
			return false
		}
		start_times.put(id, time)
		return true
	}

	fun getDuration(id: String, time: Long): Long?
	{
		val started: Long? = start_times.remove(id)
		if (started == null)
		{
			LOG.error(String.format("End event received for step %s but no start-time was found. Ignoring.", id))
			return null
		}
		
		return time - started
	}

	companion object
	{
		val LOG = LoggerFactory.getLogger(StepDurationCalculator::class.java)
	}
	
}