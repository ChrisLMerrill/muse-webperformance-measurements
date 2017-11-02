package com.webperformance.muse.measurements.stepduration

import mu.KotlinLogging
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseEventType
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.context.initializers.ContextInitializerType
import org.musetest.core.datacollection.DataCollector
import org.musetest.core.events.StepEvent
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
	
	override fun configure(configuration: ContextInitializerConfiguration)
	{
		// not expecting any configuration parameters yet
	}
	
	override fun getType(): String
	{
		return TYPE
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		context.addEventListener(this)
	}
	
	override fun getData(): StepDurations
	{
		return data
	}
	
	override fun eventRaised(event: MuseEvent)
	{
		if (StepEvent.START_TYPE.typeId == event.typeId)
		{
			val start = event as StepEvent
			if (start.stepId != null)
				startTime.put(start.stepId, start.timestampNanos)
		}
		else if (StepEvent.END_TYPE.typeId == event.typeId)
		{
			val end = event as StepEvent
			if (end.stepId != null)
			{
				val started: Long? = startTime.remove(end.stepId)
				if (started != null)
					recordDuration(end.stepId, end.timestampNanos - started)
				
			}
		}
		else if (event.type == MuseEventType.EndTest)
		{
			data.durations.iterator().forEach(operation = { measurement -> logger.error("measured: ${measurement.value}") })
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
		val TYPE = "wpi.measurements.step-durations"
	}
	
	// discovered by reflection
	@Suppress("unused")
	class StepDurationType : ContextInitializerType()
	{
		override fun getTypeId(): String
		{
			return TYPE
		}
		
		override fun getDisplayName(): String
		{
			return "Step Duration"
		}
		
		override fun getShortDescription(): String
		{
			return "Measures and collects the durations of specified steps"
		}
	}
}