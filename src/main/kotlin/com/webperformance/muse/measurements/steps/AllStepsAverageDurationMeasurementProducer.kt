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
	@Synchronized
	override fun processEvent(event: MuseEvent, step: StepConfiguration, execution_id: String)
	{
		if (StartStepEventType.TYPE_ID == event.typeId)
			recordStartTime(step, event.timestampNanos, execution_id)
		else if (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE))
			recordDuration(step, event.timestampNanos, execution_id)
	}
	
	private fun recordDuration(step: StepConfiguration, end_time: Long, execution_id: String)
	{
		val started: Long? = start_times.remove("$execution_id:${step.stepId}")
		if (started == null)
		{
			LOG.error(String.format("End event received for step %s but no start-time was found. Ignoring.", "$execution_id:${step.stepId}"))
			return
		}
		
		val duration = (end_time - started)/1000000
		total += duration
		count ++
	}

	private fun recordStartTime(step: StepConfiguration, start_time: Long, execution_id: String)
	{
		if (start_times.containsKey("$execution_id:${step.stepId}"))
			LOG.error("start-time already recorded for step $execution_id:${step.stepId}. Possibly the previous iteration never ended? Is this step called recursively (not supported at this time)? Overwriting the previous value.")
		start_times.put("$execution_id:${step.stepId}", start_time)
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val common_metadata = mutableMapOf<String, Any>()
		common_metadata.put("subject", "all-steps")
		val measurements = MeasurementsWithCommonMetadata(common_metadata)
		
		val avg : Measurement
		if (count == 0L)
			avg = Measurement(null)
		else
			avg = Measurement(total / count)
		avg.addMetadata("metric", "avg-dur")
		measurements.addMeasurement(avg)

		if (count_steps)
		{
			val count = Measurement(count)
			count.addMetadata("metric", "completed")
			measurements.addMeasurement(count)
		}
		
		total = 0
		this.count = 0
		
		return measurements
	}
	
	fun produceStepCounts(state: Boolean)
	{
		count_steps = state
	}

	private val start_times = HashMap<String, Long>()
	private var total = 0L
	private var count = 0L
	private var count_steps = false

	companion object
	{
		val LOG = LoggerFactory.getLogger(AllStepsAverageDurationMeasurementProducer::class.java)
	}
}