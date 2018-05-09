package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.*

class MultipleMeasurements() : Measurements
{
	constructor(first: Measurements) : this()
	{
		list.add(first)
	}
	
	val list = mutableListOf<Measurements>()

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