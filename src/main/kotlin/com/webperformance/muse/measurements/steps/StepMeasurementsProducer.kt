package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.step.*
import org.musetest.core.suite.*
import org.musetest.core.test.TestConfiguration

/**
 * Collects step metrics for all steps (in aggregate).
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepMeasurementsProducer(val configuration: StepMeasurementsProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private var step_tag: String? = null
	private val producers = mutableListOf<StepMeasurementProducer>()
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
        if (context !is TestSuiteExecutionContext)
            return

		step_tag = configuration.getStepTag(context)
		if (configuration.calculateOverallAverageDuration(context))
		{
			val avg_producer = AllStepsAverageDurationMeasurementProducer()
			if (configuration.countTotalSteps(context))
				avg_producer.produceStepCounts(true)
			producers.add(avg_producer)
		}
		
		if (configuration.countTotalSteps(context) && !configuration.calculateOverallAverageDuration(context))
			producers.add(StepCountMeasurementProducer())
        TestSuiteEventListener(context)
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		val accumulator = MeasurementsAccumulator()
		for (producer in producers)
			accumulator.add(producer.getMeasurements())
		return accumulator.getAll()
	}
	
	@Synchronized
	private fun processEvent(event: MuseEvent, step: StepConfiguration, test_id: String)
	{
	    for (producer in producers)
			producer.processEvent(event, step, test_id)
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
		}
	}
}