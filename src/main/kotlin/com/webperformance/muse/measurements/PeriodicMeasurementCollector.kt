package com.webperformance.muse.measurements

import org.musetest.core.MuseExecutionContext
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.suite.TestSuiteExecutionContext
import org.slf4j.LoggerFactory

/**
 * Collects average step duration metrics for all steps (in aggregate).
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
				println("Found producer: " + plugin.javaClass.simpleName)
	}
	
	private val LOG = LoggerFactory.getLogger(PeriodicMeasurementCollector::class.java)
}