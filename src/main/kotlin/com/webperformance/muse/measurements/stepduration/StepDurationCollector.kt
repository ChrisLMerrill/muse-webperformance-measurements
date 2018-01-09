package com.webperformance.muse.measurements.stepduration

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
import org.musetest.core.test.plugins.TestPluginConfiguration
import org.musetest.core.test.plugins.TestPluginType
import org.musetest.core.values.ValueSourceConfiguration
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationCollector : MuseEventListener, DataCollector
{
	private val startTime = HashMap<Long, Long>()
	private val data = StepDurations()
	private var step_tag: String? = null
	private var step_tag_source_config: ValueSourceConfiguration? = null
	private lateinit var test_context: MuseExecutionContext
	private var stepped_context: SteppedTestExecutionContext? = null
	
	override fun configure(configuration: TestPluginConfiguration)
	{
		if (configuration.parameters != null && configuration.parameters.containsKey("step-has-tag"))
			step_tag_source_config = configuration.parameters["step-has-tag"]
LOG.error("Configured")
	}
	
	override fun getType(): String
	{
		return TYPE_ID
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		stepped_context = MuseExecutionContext.findAncestor(context, SteppedTestExecutionContext::class.java)
		// TODO fail if not found!
		
		test_context = context
		context.addEventListener(this)

		step_tag_source_config?.let { config ->
			val tag_source = config.createSource(context.project)
			step_tag = tag_source.resolveValue(context).toString()
		}

LOG.error("Initialized. have test context: " + (stepped_context != null))
	}
	
	private fun findStepTestContext(context: MuseExecutionContext?): SteppedTestExecutionContext?
	{
//LOG.error("looking for stepped context in a " + context.javaClass.simpleName)
		var this_context = context
		while (this_context != null)
		{
LOG.error("context is a " + this_context.javaClass.simpleName)
			if (this_context is SteppedTestExecutionContext)
				return this_context
			this_context = this_context.parent
		}
		return null
	}
	
	override fun getData(): StepDurations
	{
		return data
	}
	
	override fun eventRaised(event: MuseEvent)
	{
LOG.error("received event: " + event.typeId)
		if (EndTestEventType.TYPE_ID.equals(event.typeId))
			test_context.removeEventListener(this)
		else if (StartStepEventType.TYPE_ID == event.typeId)
		{
			val step = findStep(event)
			if (step != null)
			{
				if (step_tag == null || step.hasTag(step_tag))
					startTime.put(step.stepId, event.timestampNanos)
			}
			else
				LOG.error("Did not find the step: " + StepEventType.getStepId(event))
		}
		else if (EndStepEventType.TYPE_ID == event.typeId)
		{
			val step = findStep(event)
			if (step != null)
			{
				if (step_tag == null || step.hasTag(step_tag))
				{
					if (!event.hasTag(StepEventType.INCOMPLETE))
					{
						val step_id = step.stepId
						val started: Long? = startTime.remove(step_id)
						if (started != null)
							data.record(step_id, (event.timestampNanos - started)/1000000)
					}
				}
			}
		}
	}
	
	private fun findStep(event: MuseEvent): StepConfiguration?
	{
		return stepped_context?.stepLocator?.findStep(StepEventType.getStepId(event))
	}
	
	companion object
	{
		val TYPE_ID = "wpi.measurements.step-durations"
	}
	
	// discovered by reflection
	@Suppress("unused")
	class StepDurationType : TestPluginType()
	{
		override fun getTypeId(): String
		{
			return TYPE_ID
		}
		
		override fun getDisplayName(): String
		{
			return "Step Duration Collector"
		}
		
		override fun getShortDescription(): String
		{
			return "Measures and collects the durations of executed steps"
		}
	}

	private val LOG = LoggerFactory.getLogger(StepDurationCollector::class.java)
}