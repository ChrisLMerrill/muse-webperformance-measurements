package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.step.*
import org.slf4j.*
import java.util.HashMap

class AllStepsAverageDurationMeasurementProducer : StepMeasurementProducer
{
	override fun processEvent(event: MuseEvent, step: StepConfiguration)
	{
		if (StartStepEventType.TYPE_ID == event.typeId)
			recordStartTime(step, event.timestampNanos)
		else if (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE))
			recordDuration(step, event.timestampNanos)
	}
	
	@Synchronized
	private fun recordDuration(step: StepConfiguration, end_time: Long)
	{
		val started: Long? = start_times.remove(step.stepId)
		if (started == null)
		{
			LOG.error(String.format("End event received for step %d but no start-time was found. Ignorning.", step.stepId))
			return;
		}
		
		val duration = (end_time - started)/1000000
		total += duration
		count ++
	}

	private fun recordStartTime(step: StepConfiguration, start_time: Long)
	{
		if (start_times.containsKey(step.stepId))
			LOG.error(String.format("start-time already recorded for step %d. Possibly the previous iteration never ended? Is this step called recursively (not supported at this time)? Overwriting the previous value.", step.stepId))
		start_times.put(step.stepId, start_time)
	}
	
	override fun getMeasurements(): Measurements
	{
		var measurement : Measurement
		if (count == 0L)
			measurement = Measurement(null)
		else
			measurement = Measurement(total / count)
		measurement.addMetadata("metric", "avg-dur")
		measurement.addMetadata("subject", "all-steps")
		
		total = 0
		count = 0
		
		return SingletonMeasurements(measurement)
	}
	
	private val start_times = HashMap<Long, Long>()
	private var total = 0L
	private var count = 0L

	private val LOG = LoggerFactory.getLogger(AllStepsAverageDurationMeasurementProducer::class.java)
}