package com.webperformance.muse.measurements

import mu.KotlinLogging
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseEventType
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.datacollection.DataCollector
import org.musetest.core.events.StepEvent
import org.musetest.core.step.StepConfiguration
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationCollector : MuseEventListener, DataCollector
{
	private val startTime = HashMap<StepConfiguration, Long>()
	private val data = StepDurations()
	
	override fun configure(configuration: ContextInitializerConfiguration?)
	{
		// not expecting any configuration parameters yet
	}
	
	override fun getType(): String
	{
		return "com.webperformance.step-durations"
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
		if (event.type == MuseEventType.StartStep)
		{
			val start = event as StepEvent
			startTime.put(start.config, start.timestampNanos)
		}
		else if (event.type == MuseEventType.EndStep)
		{
			val end = event as StepEvent
			val started: Long? = startTime.remove(end.config)
			if (started != null)
			{
				val measurement = Measurement(end.timestampNanos - started)
				data.durations.add(measurement)
			}
		}
		else if (event.type == MuseEventType.EndTest)
		{
			data.durations.iterator().forEach(operation = { measurement -> logger.error("measured: ${measurement.value}") })
		}
	}
}


