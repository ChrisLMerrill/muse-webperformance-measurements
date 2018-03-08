package com.webperformance.muse.measurements.containers

import com.webperformance.muse.measurements.*
import org.junit.*

class MeasurementsAccumulatorTests
{
	@Test
	fun empty()
	{
		val accumulator = MeasurementsAccumulator()
		val measurements = accumulator.getAll()
		Assert.assertTrue(measurements is EmptyMeasurements)
		Assert.assertFalse(measurements.iterator().hasNext())
	}
	
	@Test
	fun one()
	{
		val accumulator = MeasurementsAccumulator()
		val m1 = Measurement(1)
		accumulator.add(SingletonMeasurements(m1))
		
		val measurements = accumulator.getAll()
		val iterator = measurements.iterator()
		val out = iterator.next()
		Assert.assertTrue(m1 == out)
		Assert.assertFalse(iterator.hasNext())
	}
	
	@Test
	fun twoInOne()
	{
		val accumulator = MeasurementsAccumulator()

		val m1 = Measurement(1)
		accumulator.add(SingletonMeasurements(m1))
		val m2 = Measurement(2)
		accumulator.add(SingletonMeasurements(m2))
		
		val measurements = accumulator.getAll()
		val iterator = measurements.iterator()
		val out1 = iterator.next()
		Assert.assertTrue(m1 == out1)
		val out2 = iterator.next()
		Assert.assertTrue(m2 == out2)
		Assert.assertFalse(iterator.hasNext())
	}
	
	@Test
	fun twoInTwo()
	{
		val accumulator = MeasurementsAccumulator()

		val m1 = Measurement(1)
		val s1 = MultipleMeasurement(m1)
		val m2 = Measurement(2)
		s1.add(m2)
		accumulator.add(s1)

		val m3 = Measurement(3)
		val s2 = MultipleMeasurement(m3)
		val m4 = Measurement(4)
		s2.add(m4)
		accumulator.add(s2)
		
		val measurements = accumulator.getAll()
		val iterator = measurements.iterator()
		val out1 = iterator.next()
		Assert.assertTrue(m1 == out1)
		val out2 = iterator.next()
		Assert.assertTrue(m2 == out2)
		val out3 = iterator.next()
		Assert.assertTrue(m3 == out3)
		val out4 = iterator.next()
		Assert.assertTrue(m4 == out4)
		Assert.assertFalse(iterator.hasNext())
	}
}