package com.webperformance.muse.measurements.stepduration

import com.webperformance.muse.measurements.GoalAssessmentEvent
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.ContextInitializer
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.context.initializers.ContextInitializerType
import org.musetest.core.events.EndTestEvent
import org.musetest.core.events.EventStatus
import org.musetest.core.events.StepEvent
import org.musetest.core.step.StepExecutionStatus
import org.musetest.core.values.ValueSourceConfiguration
import java.util.*

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationGoalAssessor : MuseEventListener, ContextInitializer
{
	private val startTime = HashMap<Long, Long>()
	private var goal = 0L
	private var goal_source_config = ValueSourceConfiguration.forValue(0)
	private var test_context: MuseExecutionContext? = null
	
	override fun configure(configuration: ContextInitializerConfiguration)
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
		if (StepEvent.START_INSTANCE.typeId == event.typeId)
		{
			val start = event as StepEvent
			if (start.stepId != null)
				startTime.put(start.stepId, start.timestampNanos)
		}
		else if (StepEvent.END_INSTANCE.typeId == event.typeId)
		{
			val end = event as StepEvent
			if (end.result.status != StepExecutionStatus.INCOMPLETE && end.stepId != null)
			{
				val started: Long? = startTime.remove(end.stepId)
				if (started != null)
				{
					val duration = (end.timestampNanos - started) / 1000000  // convert to milliseconds
					var passed = true
					var message = "Goal passed"
					if (duration > goal)
						{
						passed = false
						message = "Goal failed: step duration ($duration ms) exceeded the goal ($goal ms)"
						}
					val goal_event = GoalAssessmentEvent(passed, message)
					if (!passed)
						goal_event.status = EventStatus.Failure
					test_context?.raiseEvent(goal_event)
				}
			}
		}
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
			return "Step Duration Goal Assessor"
		}
		
		override fun getShortDescription(): String
		{
			return "Records events indicating the satisfaction of the step duration against the performance goal"
		}
	}
}