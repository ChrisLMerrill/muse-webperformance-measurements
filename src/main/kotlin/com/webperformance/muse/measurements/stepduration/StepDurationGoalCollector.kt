package com.webperformance.muse.measurements.stepduration

import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseExecutionContext
import org.musetest.core.events.EndTestEvent
import org.musetest.core.events.StepEvent
import org.musetest.core.test.plugins.TestPlugin
import org.musetest.core.test.plugins.TestPluginConfiguration
import org.musetest.core.test.plugins.TestPluginType
import org.musetest.core.values.ValueSourceConfiguration
import java.util.*

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationGoalCollector : MuseEventListener, TestPlugin
{
	private val startTime = HashMap<Long, Long>()
	private val passes_map = HashMap<Long, Int>()
	private val fails_map = HashMap<Long, Int>()
	private var goal = 0L
	private var goal_source_config = ValueSourceConfiguration.forValue(0)
	private var test_context: MuseExecutionContext? = null
	
	override fun configure(configuration: TestPluginConfiguration)
	{
		if (configuration.parameters != null && configuration.parameters.containsKey("goal"))
			goal_source_config = configuration.parameters["goal"]
	}
	
	override fun getType(): String
	{
		return TYPE
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		test_context = context
		context.addEventListener(this)

		val source = goal_source_config.createSource(context.project)
		val value = source.resolveValue(context)
		when (value)
		{
			is Number -> goal = value.toLong()
		}
	}
	
	override fun eventRaised(event: MuseEvent)
	{
		if (EndTestEvent.EndTestEventType.TYPE_ID == event.typeId)
			test_context?.removeEventListener(this)
		else if (StepEvent.START_INSTANCE.typeId == event.typeId)
		{
			val start = event as StepEvent
			if (start.stepId != null)
				startTime.put(start.stepId, start.timestampNanos)
		}
		else if (StepEvent.END_INSTANCE.typeId == event.typeId)
		{
			val end = event as StepEvent
			if (end.stepId != null)
			{
				val started: Long? = startTime.remove(end.stepId)
				if (started != null)
				{
					val duration = (end.timestampNanos - started) / 1000000  // convert to milliseconds
					if (duration <= goal)
					{
						// increment the passes
						var passes = 0
						if (passes_map.containsKey(end.stepId))
							passes = passes_map.get(end.stepId)!!
						passes++
						passes_map.put(end.stepId, passes)
					}
					else
					{
						// increment the fails
						var fails = 0
						if (fails_map.containsKey(end.stepId))
							fails = fails_map.get(end.stepId)!!
						fails++
						fails_map.put(end.stepId, fails)
					}
				}
				
			}
		}
	}
	
	fun passes(stepId: Long?): Int
	{
		if (passes_map.containsKey(stepId))
			return passes_map[stepId]!!
		else
			return 0
	}

	fun fails(stepId: Long?): Int?
	{
		if (fails_map.containsKey(stepId))
			return fails_map[stepId]!!
		else
			return 0
	}

	companion object
	{
		val TYPE = "wpi.measurements.step-duration-goal-collector"
	}
	
	// discovered by reflection
	@Suppress("unused")
	class StepDurationGoalAssessorType : TestPluginType()
	{
		override fun getTypeId(): String
		{
			return TYPE
		}
		
		override fun getDisplayName(): String
		{
			return "Step Duration Goal Collector"
		}
		
		override fun getShortDescription(): String
		{
			return "Collects counts of steps that pass and fail the duration goal(s)"
		}
	}
}