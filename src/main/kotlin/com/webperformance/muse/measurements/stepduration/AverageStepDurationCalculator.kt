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
import org.musetest.core.resource.MuseInstantiationException
import org.musetest.core.step.StepConfiguration
import org.musetest.core.test.plugin.BaseTestPlugin
import org.musetest.core.values.ValueSourceConfiguration
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Collects performance measurements on all steps.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class AverageStepDurationCalculator(configuration: AverageStepDurationCalculatorConfiguration) : BaseTestPlugin(configuration), DataCollector, MuseEventListener
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
	
	override fun initialize(context: MuseExecutionContext)
	{
		stepped_context = MuseExecutionContext.findAncestor(context, SteppedTestExecutionContext::class.java)
		if (stepped_context == null)
			throw MuseInstantiationException("AverageStepDurationCalculator can only be used in stepped tests")
		
		context.addEventListener(this)

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
	
	override fun eventRaised(event: MuseEvent)
	{
		if (EndTestEventType.TYPE_ID.equals(event.typeId))
			stepped_context.removeEventListener(this)
		else if (StartStepEventType.TYPE_ID == event.typeId)
		{
			val step = findStep(event)
			if (step != null)
			{
				if (step_tag == null || step.hasTag(step_tag))
					startTime.put(step.stepId, event.timestampNanos)
			}
			else
				LOG.error("Did not find the step: " + StepEventType.getStepId(event))
		}
		else if (EndStepEventType.TYPE_ID == event.typeId && !event.hasTag(StepEventType.INCOMPLETE))
		{
			val step = findStep(event)
			if (step != null)
			{
				if (step_tag == null || step.hasTag(step_tag))
				{
					val step_id = step.stepId
					val started: Long? = startTime.remove(step_id)
					if (started != null)
						recordDuration(step_id, (event.timestampNanos - started)/1000000)
				}
			}
		else if (EndTestEventType.TYPE_ID == event.typeId)
			stepped_context.removeEventListener(this)
		}
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
	
	private fun findStep(event: MuseEvent): StepConfiguration?
	{
		return stepped_context.stepLocator?.findStep(StepEventType.getStepId(event))
	}
	
	private val LOG = LoggerFactory.getLogger(AverageStepDurationCalculator::class.java)
}