package com.webperformance.muse.measurements

import org.musetest.core.MuseExecutionContext
import org.musetest.core.events.EndSuiteEventType
import org.musetest.core.events.StartSuiteEventType
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.suite.TestSuiteExecutionContext

/**
 * A TestSuitePlugin that looks for MeasurementProducers in the context, collects measurements from them, and sends them
 * to MeasurementConsumers in the context. Periodically.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class PeriodicMeasurementCollector(configuration: PeriodicMeasurementCollectorConfiguration) : GenericConfigurablePlugin(configuration)
{
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		if (!(context is TestSuiteExecutionContext))
			return
		
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
					_thread?.interrupt()
			}
		}
		)
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
	
	
	inner class Collector : Runnable
	{
		override fun run()
		{
			var done = false
			while (!done)
			{
				val measurements = HashSet<Measurements>()
				for (producer in findProducers())
					measurements.add(producer.getMeasurements())

				for (consumer in findConsumers())
					for (each in measurements)
						consumer.acceptMeasurements(each)
				
				if (interrupted)
					done = true
				else
					try
					{
						Thread.sleep(1000)     // TODO period should be configurable
					}
					catch (e : InterruptedException)
					{
						interrupted = true
					}
			}
		}
		
		var interrupted = false
	}
}