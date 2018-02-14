package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.Measurement
import com.webperformance.muse.measurements.MeasurementConsumer
import java.io.PrintStream

class MeasurementPrinter(val outstream: PrintStream = System.out) : MeasurementConsumer
{
	override fun acceptMeasurement(measurement: Measurement)
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
		outstream.println("${measurement.value} : ${builder}")
	}
}