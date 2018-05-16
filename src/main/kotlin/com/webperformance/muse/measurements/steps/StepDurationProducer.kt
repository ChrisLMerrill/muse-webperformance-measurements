package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.step.*
import org.musetest.core.suite.*

/**
 * Collect step duration measurements for tagged steps. Expects to have the measurements collected and sent to MeasurementsConsumer(s).
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationProducer(val configuration: StepDurationProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private val calculator = StepDurationCalculator()
	private var step_tag: String? = null
	private var initialized = false
	private var measurements = MultipleMeasurement()
	
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
		
		if (!(context is TestSuiteExecutionContext))
			return
		
		initialized = true

		step_tag = configuration.getStepTag(context)
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		var collected = measurements
		measurements = MultipleMeasurement()
		return collected
	}
	
	@Synchronized
	fun processEvent(event: MuseEvent, step: StepConfiguration, test_id: String)
	{
		val id = test_id + ":" + step.stepId
		if (StartStepEventType.TYPE_ID == event.typeId)
			calculator.recordStartTime(id, event.timestamp)
		else if (EndStepEventType.TYPE_ID == event.typeId)
		{
			var duration = calculator.getDuration(id, event.timestamp)
			val measured = Measurement(duration)
			measured.addMetadata(Measurement.META_SUBJECT, "step:" + step.stepId)
			measured.addMetadata(Measurement.META_METRIC, "step.duration")
			measured.addMetadata(Measurement.META_TIMESTAMP, event.timestamp)
			measurements.add(measured)
		}
	}
	
	inner class TestEventListener(val context: SteppedTestExecutionContext) : MuseEventListener
	{
		init {
			context.addEventListener(this)
		}
		
		override fun eventRaised(event: MuseEvent)
		{
			if (EndTestEventType.TYPE_ID.equals(event.typeId))
				context.removeEventListener(this)
			else if (StartStepEventType.TYPE_ID == event.typeId
					|| (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE)))
			{
				val step = context.stepLocator.findStep(StepEventType.getStepId(event))
				if (step == null || (step_tag != null && !step.hasTag(step_tag)))
					return
				processEvent(event, step, context.hashCode().toString())
			}
			else if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
		}
	}
}