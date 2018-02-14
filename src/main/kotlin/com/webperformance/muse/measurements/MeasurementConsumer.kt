package com.webperformance.muse.measurements

interface MeasurementConsumer
{
	fun acceptMeasurement(measurement: Measurement)
}