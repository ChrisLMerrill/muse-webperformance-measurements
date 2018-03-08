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
 * Collects average step duration metrics for all steps (in aggregate).
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepMeasurementsProducer(val configuration: StepMeasurementsProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private var step_tag: String? = null
	private var initialized = false
	private val producers = mutableListOf<StepMeasurementProducer>()
	
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
		if (configuration.calculateOverallAverageDuration(context))
			producers.add(AllStepsAverageDurationMeasurementProducer())
	}
	
	override fun getMeasurements(): Measurements
	{
		val accumulator = MeasurementsAccumulator()
		for (producer in producers)
			accumulator.add(producer.getMeasurements())
		return accumulator.getAll()
	}
	
	private fun processEvent(event: MuseEvent, step: StepConfiguration)
	{
	    for (producer in producers)
			producer.processEvent(event, step)
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
				processEvent(event, step)
			}
			else if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
		}
	}
}