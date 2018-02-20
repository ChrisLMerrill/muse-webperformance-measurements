package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.Measurements
import com.webperformance.muse.measurements.MeasurementsConsumer
import java.io.PrintStream

class MeasurementsPrinter(val outstream: PrintStream = System.out) : MeasurementsConsumer
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
			outstream.println("${measurement.value} : ${builder}")
		}
	}
}