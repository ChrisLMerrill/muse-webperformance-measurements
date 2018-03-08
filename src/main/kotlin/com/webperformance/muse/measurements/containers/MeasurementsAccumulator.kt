package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.*

class MeasurementsAccumulator
{
	val _measurements = mutableListOf<Measurements>()
	
	fun add(new: Measurements)
	{
		_measurements.add(new)
	}
	
	fun getAll() : Measurements
	{
		if (_measurements.size == 0)
			return EmptyMeasurements()
		else if (_measurements.size == 1)
			return _measurements.get(0)
		else
		{
			val iterator = _measurements.iterator()
			val all = MultipleMeasurements(iterator.next())
			while (iterator.hasNext())
				all.add(iterator.next())
			return all
		}
	}
}