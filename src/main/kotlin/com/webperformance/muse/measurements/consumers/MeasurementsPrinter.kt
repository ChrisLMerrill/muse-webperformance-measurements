package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.Measurements
import com.webperformance.muse.measurements.MeasurementsConsumer
import org.musetest.core.MuseExecutionContext
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.suite.TestSuiteExecutionContext
import java.io.PrintStream
import java.util.*

class MeasurementsPrinter(val configuration: MeasurementsPrinterConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsConsumer
{
	override fun acceptMeasurements(measurements: Measurements)
	{
		for (measurement in measurements.iterator())
		{
            if (!_ignore_subjects.contains(measurement.metadata["subject"]))
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
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
        _ignore_subjects = configuration.getIgnoredSubjects(context)
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
    lateinit var _ignore_subjects : List<String>
}