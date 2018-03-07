package com.webperformance.muse.measurements.stepduration

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.step.*
import org.musetest.core.suite.*
import org.musetest.core.values.*
import org.slf4j.*
import java.util.*

/**
 * Collects average step duration metrics for all steps (in aggregate).
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class AverageStepDurationProducer(configuration: AverageStepDurationProducerConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsProducer
{
	private val totals = HashMap<Long, Long>()
	private val counts = HashMap<Long, Long>()
	private var step_tag: String? = null
	private var step_tag_source_config: ValueSourceConfiguration? = null
	private var initialized = false
	
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
			TestStepDurationCollector(context)
			return
		}
		
		if (!(context is TestSuiteExecutionContext))
			return
		
		initialized = true

		step_tag_source_config?.let { config ->
			val tag_source = config.createSource(context.project)
			step_tag = tag_source.resolveValue(context).toString()
		}
	}
	
	@Synchronized
	override fun getMeasurements(): Measurements
	{
		var total = 0L
		var count = 0L
		for (step_total in totals.values)
			total += step_total
		for (step_count in counts.values)
			count += step_count

		totals.clear()
		counts.clear()
		
		if (count == 0L)
			return MeasurementsWithCommonMetadata()
		val measurement = Measurement(total / count)
		measurement.addMetadata("metric", "avg-dur")				// TODO create metrics
		measurement.addMetadata("subject", "all-steps")  // TODO create subjects
		return MeasurementsWithCommonMetadata(measurement)
	}
	
	@Synchronized
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
	
	private val LOG = LoggerFactory.getLogger(AverageStepDurationProducer::class.java)
	
	inner class TestStepDurationCollector(val context: SteppedTestExecutionContext) : MuseEventListener
	{
		private val startTime = HashMap<Long, Long>()

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
			{
				val step_id = StepEventType.getStepId(event)
				val step = findStep(step_id)
				if (step == null || (step_tag != null && !step.hasTag(step_tag)))
					return
				val duration = calculateDuration(step_id, event.timestampNanos)
				if (duration >= 0)
					recordDuration(step_id, duration)
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
			val started: Long? = startTime.remove(step_id)
			if (started != null)
				return (finish_time - started)/1000000
			return -1
		}
		
		private fun findStep(id: Long): StepConfiguration?
		{
			return context.stepLocator.findStep(id)
		}
	}
}