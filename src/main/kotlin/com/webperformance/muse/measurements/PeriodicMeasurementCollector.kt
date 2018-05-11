package com.webperformance.muse.measurements

import com.webperformance.muse.measurements.containers.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.suite.*
import java.util.*

/**
 * A TestSuitePlugin that looks for MeasurementProducers in the context, collects measurements from them, and sends them
 * to MeasurementConsumers in the context. Periodically.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class PeriodicMeasurementCollector(val configuration: PeriodicMeasurementCollectorConfiguration) : GenericConfigurablePlugin(configuration)
{
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		if (!(context is TestSuiteExecutionContext))
			return
		setupPeriod(context)
		
		_context = context
		context.addEventListener({ event ->
			run {
				if (StartSuiteEventType.TYPE_ID.equals(event.typeId))
				{
					val new_thread = Thread(_collector)
					_thread = new_thread
					new_thread.start()
				}
				else if (EndSuiteEventType.TYPE_ID.equals(event.typeId))
				{
					_thread?.interrupt()  // TODO just set a flag finish the sample period normally...instead of interrupting?
				}
			}
		}
		)
	}
	
	private fun setupPeriod(context : MuseExecutionContext)
	{
		if (_period == 0L)
		{
			val period_config = configuration.parameters.get(PeriodicMeasurementCollectorConfiguration.PERIOD_PARAM_NAME)
			if (period_config != null)
			{
				val configured_period = period_config.createSource(context.project).resolveValue(context)
				if (configured_period != null && configured_period is Long)
					_period = configured_period
			}
			if (_period == 0L)
				_period = 10000L
		}
	}
	
	private fun findProducers() : Set<MeasurementsProducer>
	{
		if (_producers.isEmpty())
			for (plugin in _context.plugins)
				if (plugin is MeasurementsProducer)
					_producers.add(plugin)
		return _producers
	}
	
	private fun findConsumers() : Set<MeasurementsConsumer>
	{
		if (_consumers.isEmpty())
			for (plugin in _context.plugins)
				if (plugin is MeasurementsConsumer)
					_consumers.add(plugin)
		return _consumers
	}
	
	private val _producers = HashSet<MeasurementsProducer>()
	private val _consumers = HashSet<MeasurementsConsumer>()
	private val _collector = Collector()
	private lateinit var _context: TestSuiteExecutionContext
	private var _thread: Thread? = null
	private var _period = 0L
	
	
	inner class Collector : Runnable
	{
		override fun run()
		{
			var done = false
			var index = 0
			while (!done)
			{
				val sample_time = Measurement(System.currentTimeMillis())
				sample_time.addMetadata(Measurement.META_SUBJECT, "samples")
				sample_time.addMetadata(Measurement.META_METRIC, "timestamp")
				val measurements = HashSet<Measurements>()
				for (producer in findProducers())
				{
					val msmts = producer.getMeasurements()
					measurements.add(msmts)
				}

				// integrate all measurements collections into a measurements collection. Add sequence #
				val sample = MeasurementsWithCommonMetadata(sample_time)
				sample.metadata.put("sequence", index)
				for (each_measurements in measurements)
					for (each_one in each_measurements.iterator())
						sample.addMeasurement(each_one)
				
				for (consumer in findConsumers())
					consumer.acceptMeasurements(sample)
				
				if (interrupted)
					done = true
				else
					try
					{
						Thread.sleep(_period)
					}
					catch (e : InterruptedException)
					{
						interrupted = true
					}
				
				index++
			}
		}
		
		var interrupted = false
	}
}