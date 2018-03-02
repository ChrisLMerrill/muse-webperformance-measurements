package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.*

data class MeasurementsWithCommonMetadata(val metadata : MutableMap<String, Any> = mutableMapOf()) : Measurements
{
	val measurements = mutableListOf<Measurement>()
	
	constructor(measurement: Measurement) : this()
	{
		addMeasurement(measurement)
	}
	
	
	fun addMeasurement(measurement: Measurement)
	{
		measurements.add(measurement)
	}
	
	override fun iterator(): Iterator<Measurement>
	{
		val with_meta = mutableListOf<Measurement>()
		for (measurement in measurements)
		{
			val new_measurement = measurement.clone()
			for (name in metadata.keys)
			{
				val value = metadata[name]
				if (value != null)
					new_measurement.addMetadata(name, value)
			}
			with_meta.add(new_measurement)
		}
		return with_meta.iterator()
	}
}