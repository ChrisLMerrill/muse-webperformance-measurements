package com.webperformance.muse.measurements.steps

import org.slf4j.*

/*
 * Tracks the start-times of steps and returns the duration when it completes. Can also produce instantaneous data about
 * steps that have started but not finished (used for calculating running and runtime metrics
 */
class StepDurationCalculator
{
	val start_times = mutableMapOf<String, MutableMap<String,Long>>()
	
	fun recordStartTime(execution_id: String, step_id: String, time: Long): Boolean
	{
		val map = getMapForStep(step_id)
		if (map.containsKey(execution_id))
		{
			LOG.error("start-time already recorded for step $execution_id:$step_id. Possibly the previous iteration never ended? Is this step called recursively (not supported at this time)? Overwriting the previous value.")
			return false
		}
		map.put(execution_id, time)
		return true
	}

	fun getDuration(execution_id: String, step_id: String, time: Long): Long?
	{
		val map = getMapForStep(step_id)
		val started: Long? = map.remove(execution_id)
		if (started == null)
		{
			LOG.error("End event received for step $execution_id:$step_id but no start-time was found. Ignoring.")
			return null
		}
		
		return time - started
	}

	fun getRunningStepCounts(): Map<String, Long>
	{
		val counts = mutableMapOf<String, Long>()
		for (entry in start_times.entries)
			counts.put(entry.key, entry.value.size.toLong())
		return counts
	}
	
	fun getRunningStepDurations(now: Long): Map<String, Long>
	{
		val counts = mutableMapOf<String, Long>()
		for (entry in start_times.entries)
		{
			var total = 0L
			for (duration in entry.value.values)
				total += now - duration
			counts.put(entry.key, total)
		}
		return counts
	}
	
	private fun getMapForStep(step_id: String): MutableMap<String, Long>
	{
		var map = start_times.get(step_id)
		if (map == null)
		{
			map = mutableMapOf()
			start_times.put(step_id, map)
		}
		return map
	}
	
	companion object
	{
		val LOG = LoggerFactory.getLogger(StepDurationCalculator::class.java)
	}
	
}