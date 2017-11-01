package com.webperformance.muse.measurements.stepduration

import mu.KotlinLogging
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseEventType
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.ContextInitializer
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.context.initializers.ContextInitializerType
import org.musetest.core.events.StepEvent
import org.musetest.core.values.BaseValueSource
import org.musetest.core.values.ValueSourceConfiguration
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationGoalAssessor : MuseEventListener, ContextInitializer
{
	private val startTime = HashMap<Long, Long>()
	private val passes_map = HashMap<Long, Int>()
	private val fails_map = HashMap<Long, Int>()
	var goal = 0L
	var goal_source_config = ValueSourceConfiguration.forValue(0)
	
	override fun configure(configuration: ContextInitializerConfiguration)
	{
		if (configuration.parameters != null && configuration.parameters.containsKey("goal"))
			goal_source_config = configuration.parameters["goal"];
	}
	
	override fun getType(): String
	{
		return TYPE
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
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
		if (event.type == MuseEventType.StartStep)
		{
			val start = event as StepEvent
			if (start.stepId != null)
				startTime.put(start.stepId, start.timestampNanos)
		}
		else if (event.type == MuseEventType.EndStep)
		{
			val end = event as StepEvent
			if (end.stepId != null)
			{
				val started: Long? = startTime.remove(end.stepId)
				if (started != null)
				{
					val duration = end.timestampNanos - started
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
		val TYPE = "wpi.measurements.step-duration-goal-assessor"
	}
	
	// discovered by reflection
	@Suppress("unused")
	class StepDurationGoalAssessorType : ContextInitializerType()
	{
		override fun getTypeId(): String
		{
			return TYPE
		}
		
		override fun getDisplayName(): String
		{
			return "Step Duration Goal Assessment"
		}
		
		override fun getShortDescription(): String
		{
			return "Assesses the step duration against the performance goal"
		}
	}
}