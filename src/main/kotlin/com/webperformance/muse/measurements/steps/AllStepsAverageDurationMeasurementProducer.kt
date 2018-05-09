package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.step.*

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
		calculator.recordFinish("$execution_id:${step.stepId}", end_time)
	}

	private fun recordStartTime(step: StepConfiguration, start_time: Long, execution_id: String)
	{
		calculator.recordStartTime("$execution_id:${step.stepId}", start_time)
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val common_metadata = mutableMapOf<String, Any>()
		common_metadata.put("subject", "all-steps")
		val measurements = MeasurementsWithCommonMetadata(common_metadata)
		
		val calculated = calculator.calculateAndReset()
		val avg : Measurement
		if (calculated.count == 0L)
			avg = Measurement(null)
		else
			avg = Measurement(calculated.average)
		avg.addMetadata("metric", "avg-dur")
		measurements.addMeasurement(avg)

		if (count_steps)
		{
			val count = Measurement(calculated.count)
			count.addMetadata("metric", "completed")
			measurements.addMeasurement(count)
		}
		
		return measurements
	}
	
	fun produceStepCounts(state: Boolean)
	{
		count_steps = state
	}

	private val calculator = StepAverageDurationCalculator()
	private var count_steps = false
}