package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.*

class MultipleMeasurement(first: Measurement) : Measurements
{
	val list = mutableListOf<Measurement>()
	
	init
	{
		list.add(first)
	}
	
	fun add(next: Measurement)
	{
		list.add(next)
	}
	
	override fun iterator(): Iterator<Measurement>
	{
		return list.iterator()
	}
}