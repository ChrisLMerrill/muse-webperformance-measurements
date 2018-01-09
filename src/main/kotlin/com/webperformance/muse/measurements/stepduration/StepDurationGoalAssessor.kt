package com.webperformance.muse.measurements.stepduration

import com.webperformance.muse.measurements.GoalAssessmentEventType
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.SteppedTestExecutionContext
import org.musetest.core.datacollection.DataCollector
import org.musetest.core.events.EndStepEventType
import org.musetest.core.events.EndTestEventType
import org.musetest.core.events.StartStepEventType
import org.musetest.core.events.StepEventType
import org.musetest.core.step.StepConfiguration
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
class StepDurationGoalAssessor : MuseEventListener, TestPlugin, DataCollector
{
	private val startTime = HashMap<Long, Long>()
	private var goal = 0L
	private var goal_source_config = ValueSourceConfiguration.forValue(0)
	private var step_tag: String? = null
	private var step_tag_source_config: ValueSourceConfiguration? = null
	private var step_goal_name: String? = null
	private var step_goal_name_source_config: ValueSourceConfiguration? = null
	private var collect_goals_config: ValueSourceConfiguration? = null
	private var stepped_context: SteppedTestExecutionContext? = null
	private var goals: StepDurationGoals? = null
	
	override fun configure(configuration: TestPluginConfiguration)
	{
		if (configuration.parameters != null && configuration.parameters.containsKey("goal"))
			goal_source_config = configuration.parameters["goal"]
		if (configuration.parameters != null && configuration.parameters.containsKey("step-has-tag"))
			step_tag_source_config = configuration.parameters["step-has-tag"]
		if (configuration.parameters != null && configuration.parameters.containsKey("step-goal-name"))
			step_goal_name_source_config = configuration.parameters["step-goal-name"]
		if (configuration.parameters != null && configuration.parameters.containsKey("collect-goals"))
			collect_goals_config = configuration.parameters["collect-goals"]
	}
	
	override fun getType(): String = TYPE
	
	override fun initialize(context: MuseExecutionContext)
	{
		stepped_context = MuseExecutionContext.findAncestor(context, SteppedTestExecutionContext::class.java)
		// TODO throw exception if not found
		context.addEventListener(this)
		
		val source = goal_source_config.createSource(context.project)
		val value = source.resolveValue(context)
		when (value)
		{
			is Number -> goal = value.toLong()
		}
		
		step_tag_source_config?.let { config ->
			val tag_source = config.createSource(context.project)
			step_tag = tag_source.resolveValue(context).toString()
		}
		step_goal_name_source_config?.let { config ->
			val name_source = config.createSource(context.project)
			step_goal_name = name_source.resolveValue(context).toString()
		}
		collect_goals_config?.let { config ->
			val collect_source = config.createSource(context.project)
			val collect = collect_source.resolveValue(context)
			if (collect is Boolean && collect)
				goals = StepDurationGoals()
		}
	}
	
	override fun eventRaised(event: MuseEvent)
	{
		if (EndTestEventType.TYPE_ID == event.typeId)
			stepped_context?.removeEventListener(this)
		else if (StartStepEventType.TYPE_ID == event.typeId)
		{
			val step = findStep(event)
			if (step != null)
			{
				val step_id = step.stepId
				if (step_tag == null || step.hasTag(step_tag))
					startTime.put(step_id, event.timestampNanos)
			}
		}
		else if (EndStepEventType.TYPE_ID == event.typeId)
		{
			if (!event.hasTag(StepEventType.INCOMPLETE))
			{
				val step = findStep(event)
				if (step != null)
				{
					val step_id = StepEventType.getStepId(event)
					val started: Long? = startTime.remove(step_id)
					if (started != null)
					{
						var duration_goal = goal
						if (step_goal_name != null && step.getMetadataField(step_goal_name) != null)
							duration_goal = step.getMetadataField(step_goal_name).toString().toLong()
						
						val duration = (event.timestampNanos - started) / 1000000  // convert to milliseconds
						var passed = true
						var message = "Goal passed"
						if (duration > duration_goal)
						{
							passed = false
							message = "Goal failed: step duration ($duration ms) exceeded the goal ($duration_goal ms)"
						}
						
						goals?.record(step_id, passed)
						stepped_context?.raiseEvent(GoalAssessmentEventType.create(passed, message))
					}
				}
			}
		}
	}
	
	private fun findStep(event: MuseEvent): StepConfiguration?
	{
		return stepped_context?.stepLocator?.findStep(StepEventType.getStepId(event))
	}
	
	override fun getData(): StepDurationGoals?
	{
		return goals
	}
	
	companion object
	{
		val TYPE = "wpi.measurements.step-duration-goal-assessor"
	}
	
	// discovered by reflection
	@Suppress("unused")
	class StepDurationGoalAssessorType : TestPluginType()
	{
		override fun getTypeId(): String = TYPE
		override fun getDisplayName(): String = "Step Duration Goal Assessor"
		override fun getShortDescription(): String = "Records events indicating the satisfaction of the step duration against the performance goal"
	}
}