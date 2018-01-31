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
import org.musetest.core.resource.MuseInstantiationException
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
	private val startTime = HashMap<Long, Long>()
	private val totals = HashMap<Long, Long>()
	private val counts = HashMap<Long, Long>()
	private var step_tag: String? = null
	private var step_tag_source_config: ValueSourceConfiguration? = null
	private lateinit var stepped_context: SteppedTestExecutionContext
	
	init {
		if (configuration.parameters != null && configuration.parameters.containsKey("steptag"))
			step_tag_source_config = configuration.parameters["steptag"]
	}
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		stepped_context = MuseExecutionContext.findAncestor(context, SteppedTestExecutionContext::class.java)
		if (stepped_context == null)
			throw MuseInstantiationException("AverageStepDurationCalculator can only be used in stepped tests")
		
		EventListener(context)

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
	
	@Synchronized private fun recordStartTime(step_id: Long, start_time: Long)
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
	
	@Synchronized private fun handleStepCompletion(step_id: Long, finish_time: Long)
	{
		val step = findStep(step_id)
		if (step != null)
		{
			if (step_tag == null || step.hasTag(step_tag))
			{
				val started: Long? = startTime.remove(step_id)
				if (started != null)
					recordDuration(step_id, (finish_time - started)/1000000)
			}
		}
	}
	
	private fun findStep(id: Long): StepConfiguration?
	{
		return stepped_context.stepLocator?.findStep(id)
	}
	
	private val LOG = LoggerFactory.getLogger(AverageStepDurationCalculator::class.java)
	
	inner class EventListener(val context: MuseExecutionContext) : MuseEventListener
	{
		init {
			context.addEventListener(this)
		}
		
		override fun eventRaised(event: MuseEvent)
		{
			if (EndTestEventType.TYPE_ID.equals(event.typeId))
				context.removeEventListener(this)
			else if (StartStepEventType.TYPE_ID == event.typeId)
				recordStartTime(StepEventType.getStepId(event), event.timestampNanos)
			else if (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE))
				handleStepCompletion(StepEventType.getStepId(event), event.timestampNanos)
			else if (EndTestEventType.TYPE_ID == event.typeId)
				context.removeEventListener(this)
		}
	}
}