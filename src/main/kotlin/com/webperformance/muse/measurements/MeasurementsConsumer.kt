package com.webperformance.muse.measurements

interface MeasurementsConsumer
{
	fun acceptMeasurements(measurements: Measurements)
}