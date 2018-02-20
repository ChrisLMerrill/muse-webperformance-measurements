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

		for (plugin in context.plugins)
			if (plugin is MeasurementsProducer)
				producers.add(plugin)
		
		context.addEventListener({ event ->
			run {
				if (StartSuiteEventType.TYPE_ID.equals(event.typeId) && (producers.size > 0))
				{
					val new_thread = Thread(collector)
					thread = new_thread
					new_thread.start()
				}
				else if (EndSuiteEventType.TYPE_ID.equals(event.typeId))
					thread?.interrupt()
			}
		}
		)
	}
	
	private val producers = HashSet<MeasurementsProducer>()
	private val collector = Collector()
	private var thread : Thread? = null
	
	
	inner class Collector : Runnable
	{
		override fun run()
		{
			var done = false
			while (!done)
			{
				for (producer in producers)
				{
					for (measurement in producer.getMeasurements().iterator())
						println("collected measurement: ${measurement}")  // TODO send it somewhere
				}
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