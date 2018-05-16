package com.webperformance.muse.measurements.stepduration

import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventListener
import org.musetest.core.MuseExecutionContext
import org.musetest.core.context.SteppedTestExecutionContext
import org.musetest.core.datacollection.DataCollector
import org.musetest.core.events.EndStepEventType
import org.musetest.core.events.EndTestEventType
import org.musetest.core.events.StartStepEventType
import org.musetest.core.events.StepEventType
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.plugins.MusePlugin
import org.musetest.core.step.StepConfiguration
import org.musetest.core.suite.TestSuiteExecutionContext
import org.musetest.core.values.ValueSourceConfiguration
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class AverageStepDurationCalculator(configuration: AverageStepDurationCalculatorConfiguration) : GenericConfigurablePlugin(configuration), DataCollector
{
	private val totals = HashMap<Long, Long>()
	private val counts = HashMap<Long, Long>()
	private var step_tag: String? = null
	private var step_tag_source_config: ValueSourceConfiguration? = null
	
	init {
		if (configuration.parameters != null && configuration.parameters.containsKey("steptag"))
			step_tag_source_config = configuration.parameters["steptag"]
	}
	
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
			context.addPlugin(this)
		else if (context is SteppedTestExecutionContext)
			context.addPlugin(TestStepDurationCollector())
		return true
	}
	
	override fun applyToContextType(context: MuseExecutionContext): Boolean
	{
		return context is TestSuiteExecutionContext || context is SteppedTestExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		if (!(context is TestSuiteExecutionContext))
			return

		step_tag_source_config?.let { config ->
			val tag_source = config.createSource(context.project)
			step_tag = tag_source.resolveValue(context).toString()
		}
	}
	
	override fun getData(): AverageStepDurations
	{
		val data = AverageStepDurations()
		for (step_id in totals.keys)
		{
			val average = totals[step_id]
			val count = counts[step_id]
			if (average != null && count != null)
				data.averages.put(step_id, average / count)
		}
		return data
	}
	
	private fun recordDuration(step_id: Long, duration: Long)
	{
		var total = totals[step_id]
		if (total == null)
			total = duration
		else
			total += duration
		totals.put(step_id, total)
		
		var count = counts[step_id]
		if (count == null)
			count = 1
		else
			count++
		counts.put(step_id, count)
	}
	
	private val LOG = LoggerFactory.getLogger(AverageStepDurationCalculator::class.java)
	
	inner class TestStepDurationCollector : MusePlugin, MuseEventListener
	{
		lateinit var context : SteppedTestExecutionContext
		private val startTime = HashMap<Long, Long>()
		
		override fun conditionallyAddToContext(context: MuseExecutionContext?, automatic: Boolean): Boolean
		{
			return false
		}
		
		override fun initialize(the_context: MuseExecutionContext)
		{
			if (the_context is SteppedTestExecutionContext)
			{
				context = the_context
				context.addEventListener(this)
			}
		}
		
		override fun eventRaised(event: MuseEvent)
		{
			if (EndTestEventType.TYPE_ID.equals(event.typeId))
				context.removeEventListener(this)
			else if (StartStepEventType.TYPE_ID == event.typeId)
				recordStartTime(StepEventType.getStepId(event), event.timestamp)
			else if (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE))
			{
				val duration = calculateDuration(StepEventType.getStepId(event), event.timestamp)
				if (duration >= 0)
					recordDuration(StepEventType.getStepId(event), duration)
			}
			else if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
		}

		private fun recordStartTime(step_id: Long, start_time: Long)
		{
			val step = findStep(step_id)
			if (step != null)
			{
				if (step_tag == null || step.hasTag(step_tag))
					startTime.put(step.stepId, start_time)
			}
			else
				LOG.error("Did not find the step: " + step_id)
		}
		
		private fun calculateDuration(step_id: Long, finish_time: Long): Long
		{
			val step = findStep(step_id)
			if (step != null)
			{
				if (step_tag == null || step.hasTag(step_tag))
				{
					val started: Long? = startTime.remove(step_id)
					if (started != null)
						return finish_time - started
				}
			}
			return -1
		}
		
		private fun findStep(id: Long): StepConfiguration?
		{
			return context.stepLocator?.findStep(id)
		}
		
	}
}