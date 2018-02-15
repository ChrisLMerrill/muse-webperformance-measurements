package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.Measurement
import com.webperformance.muse.measurements.Measurements
import java.util.*

class SingletonMeasurements(val single : Measurement) : Measurements
{
	override fun iterator(): Iterator<Measurement>
	{
		return Collections.singleton(single).iterator()
	}
}