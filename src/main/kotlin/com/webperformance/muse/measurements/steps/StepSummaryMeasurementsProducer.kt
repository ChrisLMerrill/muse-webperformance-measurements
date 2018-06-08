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
class StepSummaryMeasurementsProducer(val configuration: StepSummaryMeasurementsProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private val calculator = StepSummaryMeasurementsCalculator()
	private var step_tag: String? = null
	private var add_test_id = false
	private var count_successes = false
	private var count_failures = false
	private var count_errors = false
	private var collect_running_steps = false
	private var initialized = false
	private lateinit var descriptors: StepDescriptors
	private lateinit var suite_context : TestSuiteExecutionContext
	
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
		suite_context = context
		step_tag = configuration.getStepTag(context)
		add_test_id = configuration.isAddTestId(context)
		collect_running_steps = configuration.isCollectRunningSteps(context)
		count_successes = configuration.isCollectSuccesses(context)
		count_failures = configuration.isCollectFailures(context)
		count_errors = configuration.isCollectErrors(context)
		descriptors = context.getProject().stepDescriptors
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val collected = MultipleMeasurement()
		val timestamp = System.currentTimeMillis()
		
		
		for (step_counters in calculator.extractAllCounts())
		{
			var test_id: String? = null
			if (add_test_id)
				test_id = step_counters.test_id
			if (count_successes)
				collected.add(createMeasurement(step_counters.step_id.toString(), SUCCESS_COUNT, step_counters.successes, timestamp, test_id))
			if (count_failures)
				collected.add(createMeasurement(step_counters.step_id.toString(), FAILURE_COUNT, step_counters.failures, timestamp, test_id))
			if (count_errors)
				collected.add(createMeasurement(step_counters.step_id.toString(), ERROR_COUNT, step_counters.errors, timestamp, test_id))
			if (collect_running_steps)
				collected.add(createMeasurement(step_counters.step_id.toString(), RUNNING_COUNT, step_counters.running, timestamp, test_id))
		}

		return collected
	}
	
	@Synchronized
	fun processEvent(event: MuseEvent, step: StepConfiguration, test_id: String)
	{
		if (StartStepEventType.TYPE_ID == event.typeId)
			 calculator.startStep(step.stepId, test_id)
		else if (EndStepEventType.TYPE_ID == event.typeId)
			calculator.endStep(step.stepId, test_id, event.hasTag(MuseEvent.FAILURE), event.hasTag(MuseEvent.ERROR))
	}
	
	private fun createMeasurement(step_id: String, measurement: String, value: Number, timestamp: Long, test_id: String?): Measurement
	{
		val measured = Measurement(value)
		measured.addMetadata(Measurement.META_SUBJECT, step_id)
		measured.addMetadata(Measurement.META_SUBJECT_TYPE, SUBJECT_TYPE)
		measured.addMetadata(Measurement.META_METRIC, measurement)
		measured.addMetadata(Measurement.META_TIMESTAMP, timestamp)
		if (add_test_id && test_id != null)
			measured.addMetadata(Measurement.META_TEST, test_id)
		return measured
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
				processEvent(event, step, context.test.id)
			}
			else if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
		}
	}
	
	companion object
	{
		val SUBJECT_TYPE = "step"
		val FAILURE_COUNT = "failure"
		val ERROR_COUNT = "error"
		val SUCCESS_COUNT = "success"
		val RUNNING_COUNT = "running"
	}
}