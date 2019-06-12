package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.Measurement
import com.webperformance.muse.measurements.Measurements
import com.webperformance.muse.measurements.MeasurementsProducer
import com.webperformance.muse.measurements.TaskDurationCalculator
import com.webperformance.muse.measurements.containers.MultipleMeasurement
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.SteppedTestExecutionContext
import org.musetest.core.events.*
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.step.StepConfiguration
import org.musetest.core.step.descriptor.StepDescriptors
import org.musetest.core.suite.TestSuiteExecutionContext
import org.musetest.core.test.TestConfiguration

/**
 * Collect step duration measurements for tagged steps. Expects to have the measurements collected and sent to MeasurementsConsumer(s).
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationProducer(val configuration: StepDurationProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private val calculator = TaskDurationCalculator()
	private var step_tag: String? = null
	private var add_test_id = false
	private var add_status = false
	private var initialized = false
	private var measurements = MultipleMeasurement()
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
		
        context.addPlugin(this)
        return true
	}
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		if (context !is TestSuiteExecutionContext)
			return
		
		initialized = true
		suite_context = context
		step_tag = configuration.getStepTag(context)
		add_test_id = configuration.isAddTestId(context)
		add_status = configuration.isAddStepStatus(context)
		descriptors = context.getProject().stepDescriptors

        TestSuiteEventListener(context)
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val collected = measurements
		measurements = MultipleMeasurement()
		
		if (configuration.isCollectRunningSteps(suite_context))
		{
			val timestamp = System.currentTimeMillis()
			val counts = calculator.getRunningTaskCounts()
			for (step_id in counts.keys)
				collected.add(createMeasurement(step_id, "running", counts.getValue(step_id), timestamp, null))
			val durations = calculator.getRunningTaskDurations(timestamp)
			for (step_id in durations.keys)
				collected.add(createMeasurement(step_id, "running_duration", durations.getValue(step_id), timestamp, null))
		}

		return collected
	}
	
	fun processEvent(event: MuseEvent, step: StepConfiguration, context_id: String, test_id: String)
	{
		if (StartStepEventType.TYPE_ID == event.typeId)
            calculator.recordStartTime(context_id, step.stepId.toString(), event.timestamp)
		else if (EndStepEventType.TYPE_ID == event.typeId)
		{
			val step_id = step.stepId.toString()
			val duration = calculator.getDuration(context_id, step_id, event.timestamp)
			if (duration != null)
			{
				val measurement = createMeasurement(step_id, "duration", duration, event.timestamp, test_id)
				if (add_status)
				{
                    when {
                        event.hasTag(MuseEvent.FAILURE) -> measurement.addMetadata(Measurement.META_STATUS, FAILURE)
                        event.hasTag(MuseEvent.ERROR) -> measurement.addMetadata(Measurement.META_STATUS, ERROR)
                        else -> measurement.addMetadata(Measurement.META_STATUS, SUCCESS)
                    }
				}
				measurements.add(measurement)
			}
		}
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

    inner class TestSuiteEventListener(val context: TestSuiteExecutionContext) : MuseEventListener
    {
        init
        {
            context.addEventListener(this)
        }

        override fun eventRaised(event: MuseEvent)
        {
            if (StartSuiteTestEventType.TYPE_ID == event.typeId)
            {
                val config_var = StartSuiteTestEventType.getConfigVariableName(event)
                val config = context.getVariable(config_var) as TestConfiguration
                val test_context = config.context()
                if (test_context is SteppedTestExecutionContext)
                    TestEventListener(test_context)
            }
            else if (EndSuiteEventType.TYPE_ID == event.typeId)
                context.removeEventListener(this)
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
		}
	}
	
	companion object
	{
		const val SUBJECT_TYPE = "step"
		const val FAILURE = "failure"
		const val ERROR = "error"
		const val SUCCESS = "success"
	}
}