package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.Measurements
import com.webperformance.muse.measurements.MeasurementsConsumer
import org.musetest.core.MuseExecutionContext
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.resource.generic.GenericResourceConfiguration
import org.musetest.core.suite.TestSuiteExecutionContext
import java.io.PrintStream

class MeasurementsPrinter(configuration: GenericResourceConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsConsumer
{
	override fun acceptMeasurements(measurements: Measurements)
	{
		for (measurement in measurements.iterator())
		{
			val builder = StringBuilder()
			for (name in measurement.metadata.keys)
			{
				if (builder.length > 0)
					builder.append(", ")
				builder.append(name)
				builder.append("=")
				builder.append(measurement.metadata[name])
			}
			getStream().println("${measurement.value} : ${builder}")
		}
	}
	
	override fun initialize(context: MuseExecutionContext?)
	{
		// here would be the place to read the configuration and determine where to send the output
	}
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	private fun getStream() : PrintStream
		{
		return _stream
		}
	
	fun setStream(stream: PrintStream)
	{
		_stream = stream;
	}
	
	var _stream = System.out
}