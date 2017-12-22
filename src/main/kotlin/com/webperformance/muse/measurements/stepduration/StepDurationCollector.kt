package com.webperformance.muse.measurements.stepduration

import mu.KotlinLogging
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseExecutionContext
import org.musetest.core.datacollection.DataCollector
import org.musetest.core.events.EndTestEvent
import org.musetest.core.events.StepEvent
import org.musetest.core.test.plugins.TestPluginConfiguration
import org.musetest.core.test.plugins.TestPluginType
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationCollector : MuseEventListener, DataCollector
{
	private val startTime = HashMap<Long, Long>()
	private val data = StepDurations()
	private var test_context : MuseExecutionContext? = null
	
	override fun configure(configuration: TestPluginConfiguration)
	{
		// not expecting any configuration parameters yet
	}
	
	override fun getType(): String
	{
		return TYPE_ID
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		test_context = context
		context.addEventListener(this)
	}
	
	override fun getData(): StepDurations
	{
		return data
	}
	
	override fun eventRaised(event: MuseEvent)
	{
		if (StepEvent.START_INSTANCE.typeId == event.typeId)
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
					recordDuration(end.stepId, (end.timestampNanos - started)/1000000)
				
			}
		}
		else if (event.typeId == EndTestEvent.EndTestEventType.TYPE_ID)
		{
			data.durations.iterator().forEach(operation = { measurement -> logger.error("measured: ${measurement.value}") })
			test_context?.removeEventListener(this)
		}
	}
	
	private fun recordDuration(stepid: Long, duration: Long)
	{
		var list = data.durations.get(stepid)
		if (list == null)
		{
			list = mutableListOf()
			data.durations.put(stepid, list)
		}
		list.add(duration)
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
}