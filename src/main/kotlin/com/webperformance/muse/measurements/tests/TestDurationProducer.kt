package com.webperformance.muse.measurements.tests

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.suite.*

/**
 * Collect test duration measurements. Expects to have the measurements collected and sent to MeasurementsConsumer(s).
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class TestDurationProducer(val configuration: TestDurationProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private val calculator = TaskDurationCalculator()
	private var initialized = false
	private var measurements = MultipleMeasurement()
	private var collect_running_measurements = false
	
	override fun conditionallyAddToContext(context: MuseExecutionContext, automatic: Boolean): Boolean
	{
		if (!applyToContextType(context))
			return false
		if (automatic)
		{
			if (!applyAutomatically(context))
				return false
		}
		if (!applyToThisTest(context))
			return false
		
		if (context is TestSuiteExecutionContext)
		{
			context.addPlugin(this)
			return true
		}
		return false
	}
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext || context is SteppedTestExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		if (context is SteppedTestExecutionContext && initialized)
		{
			TestEventListener(context)
			return
		}
		

		if (context !is TestSuiteExecutionContext)
			return
		
		initialized = true
		collect_running_measurements = configuration.isCollectRunningTests(context)
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val collected = measurements
		measurements = MultipleMeasurement()
		
		if (collect_running_measurements)
		{
			val timestamp = System.currentTimeMillis()
			val counts = calculator.getRunningTaskCounts()
			for (test_id in counts.keys)
				collected.add(createMeasurement(test_id, "running", counts[test_id]!!, timestamp))
			val durations = calculator.getRunningTaskDurations(timestamp)
			for (test_id in durations.keys)
				collected.add(createMeasurement(test_id, "running_duration", durations[test_id]!!, timestamp))
		}
		
		return collected
	}
	
	@Synchronized
	fun processEvent(event: MuseEvent, context: SteppedTestExecutionContext)
	{
		if (StartTestEventType.TYPE_ID == event.typeId)
			calculator.recordStartTime(context.testExecutionId, context.test.id, event.timestamp)
		else if (EndTestEventType.TYPE_ID == event.typeId)
		{
			val duration = calculator.getDuration(context.testExecutionId, context.test.id, event.timestamp)
			if (duration != null)
				measurements.add(createMeasurement(context.test.id, "duration", duration, event.timestamp))
		}
	}


	private fun createMeasurement(test_id: String, measurement: String, value: Number, timestamp: Long): Measurement
	{
		val measured = Measurement(value)
		measured.addMetadata(Measurement.META_SUBJECT, test_id)
		measured.addMetadata(Measurement.META_SUBJECT_TYPE, "test")
		measured.addMetadata(Measurement.META_METRIC, measurement)
		measured.addMetadata(Measurement.META_TIMESTAMP, timestamp)
		return measured
	}
	
	inner class TestEventListener(val context: SteppedTestExecutionContext) : MuseEventListener
	{
		init
		{
			context.addEventListener(this)
		}
		
		override fun eventRaised(event: MuseEvent)
		{
			if (StartTestEventType.TYPE_ID == event.typeId)
				processEvent(event, context)
			else if (EndTestEventType.TYPE_ID == event.typeId)
			{
				context.removeEventListener(this)
				processEvent(event, context)
			}
		}
	}
}