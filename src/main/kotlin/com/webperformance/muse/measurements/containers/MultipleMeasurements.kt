package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.*

class MultipleMeasurements(first: Measurements) : Measurements
{
	val list = mutableListOf<Measurements>()
	
	init {
		list.add(first)
	}
	
	fun add(more : Measurements)
	{
		list.add(more)
	}
	
	override fun iterator(): Iterator<Measurement>
	{
		val all = mutableListOf<Measurement>()
		for (measurements in list)
		{
			for (measurement in measurements.iterator())
				all.add(measurement)
		}
		return all.iterator()
	}
	
}