package com.webperformance.muse.measurements

import org.slf4j.*

/*
 * Tracks the start-times of actions and returns the duration when they completes. Can also produce instantaneous data about
 * actions that have started but not finished (used for calculating running and runtime metrics)
 */
class TaskDurationCalculator
{
	val start_times = mutableMapOf<String, MutableMap<String,Long>>()
	
	fun recordStartTime(instance_id: String, task_id: String, time: Long): Boolean
	{
		val map = getMapForTask(task_id)
		if (map.containsKey(instance_id))
		{
			LOG.error("start-time already recorded for task $instance_id:$task_id. Possibly the previous iteration never ended? Is this task called recursively (not supported at this time)? Overwriting the previous value.")
			return false
		}
		map.put(instance_id, time)
		return true
	}

	/**
	 * @param instance_id The id of this specific instance of the task (because the task may be in progress several times, simultaneously)
	 * @param task_id The id of the task being executed. The same task (e.g. 
	 */
	fun getDuration(instance_id: String, task_id: String, time: Long): Long?
	{
		val map = getMapForTask(task_id)
		val started: Long? = map.remove(instance_id)
		if (started == null)
		{
			LOG.error("End event received for task $instance_id:$task_id but no start-time was found. Ignoring.")
			return null
		}
		
		return time - started
	}

	fun getRunningTaskCounts(): Map<String, Long>
	{
		val counts = mutableMapOf<String, Long>()
		for (entry in start_times.entries)
		{
			val count = entry.value.size
			if (count > 0)
				counts.put(entry.key, count.toLong())
		}
		return counts
	}
	
	fun getRunningTaskDurations(now: Long): Map<String, Long>
	{
		val durations = mutableMapOf<String, Long>()
		for (entry in start_times.entries)
		{
			if (entry.value.size > 0)
			{
				var total = 0L
				for (duration in entry.value.values)
					total += now - duration
				durations.put(entry.key, total)
			}
		}
		return durations
	}
	
	private fun getMapForTask(task_id: String): MutableMap<String, Long>
	{
		var map = start_times.get(task_id)
		if (map == null)
		{
			map = mutableMapOf()
			start_times.put(task_id, map)
		}
		return map
	}
	
	companion object
	{
		val LOG = LoggerFactory.getLogger(TaskDurationCalculator::class.java)
	}
	
}