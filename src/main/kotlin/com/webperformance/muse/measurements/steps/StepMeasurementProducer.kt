package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import org.musetest.core.*
import org.musetest.core.step.*

interface StepMeasurementProducer : MeasurementsProducer
{
	fun processEvent(event: MuseEvent, step: StepConfiguration, execution_id: String)
}