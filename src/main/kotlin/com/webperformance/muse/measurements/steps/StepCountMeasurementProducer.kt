package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.step.*

class StepCountMeasurementProducer : StepMeasurementProducer
{
	override fun processEvent(event: MuseEvent, step: StepConfiguration, execution_id: String)
	{
		if (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE))
			count++
	}
	
	override fun getMeasurements(): Measurements
	{
		val measurement = Measurement(count)
		measurement.addMetadata("metric", "completed")
		measurement.addMetadata("subject", "all-steps")
		
		count = 0
		
		return SingletonMeasurements(measurement)
	}
	
	private var count = 0L
}