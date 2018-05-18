package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.step.*
import org.musetest.core.step.descriptor.*
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
	private var add_test_id = false
	private var initialized = false
	private var measurements = MultipleMeasurement()
	private lateinit var descriptors: StepDescriptors
	
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
		step_tag = configuration.getStepTag(context)
		add_test_id = configuration.isAddTestId(context)
		descriptors = context.getProject().stepDescriptors
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val collected = measurements
		measurements = MultipleMeasurement()
		return collected
	}
	
	@Synchronized
	fun processEvent(event: MuseEvent, step: StepConfiguration, context_id: String, test_id: String)
	{
		val id = context_id + ":" + step.stepId
		if (StartStepEventType.TYPE_ID == event.typeId)
			calculator.recordStartTime(id, event.timestamp)
		else if (EndStepEventType.TYPE_ID == event.typeId)
		{
			val duration = calculator.getDuration(id, event.timestamp)
			val measured = Measurement(duration)
			measured.addMetadata(Measurement.META_SUBJECT, step.stepId.toString())
			measured.addMetadata(Measurement.META_SUBJECT_TYPE, "step")
			measured.addMetadata(Measurement.META_METRIC, "duration")
			measured.addMetadata(Measurement.META_TIMESTAMP, event.timestamp)
			if (add_test_id)
				measured.addMetadata(Measurement.META_TEST, test_id)
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
			if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
			else if (StartStepEventType.TYPE_ID == event.typeId
					|| (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE)))
			{
				val step = context.stepLocator.findStep(StepEventType.getStepId(event))
				if (step == null || (step_tag != null && !step.hasTag(step_tag)))
					return
				processEvent(event, step, context.hashCode().toString(), context.test.id)
			}
			else if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
		}
	}
}