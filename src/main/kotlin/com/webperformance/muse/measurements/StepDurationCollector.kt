package com.webperformance.muse.measurements

import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.step.*

import java.util.*

/**
 * Collects performance com.webperformance.muse.measurements on steps tagged for collection.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationCollector : MuseEventListener, DataCollector
	{
	val measurements: MutableList<Measurement> = mutableListOf()
	private val startTime = HashMap<StepConfiguration, Long>()

	override fun initialize(context: MuseExecutionContext)
		{
		context.addEventListener(this)
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
			val started : Long? = startTime.remove(end.config)
			if (started != null)
				{
				val measurement = Measurement(end.timestampNanos - started)
				measurements.add(measurement)
				}
			}
		}
	}


