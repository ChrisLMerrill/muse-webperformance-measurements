package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.Measurement
import com.webperformance.muse.measurements.containers.SingletonMeasurements
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MeasurementPrinterTests
{
	@Test
	fun plainValue()
	{
		val m = Measurement(123)
		printer.acceptMeasurements(SingletonMeasurements(m))
		
		val result = outstream.toString()
		Assert.assertTrue(result.contains("123"))
	}
	
	@Test
	fun oneAttribute()
	{
		val m = Measurement(123)
		m.metadata["a1"] = "v1"
		printer.acceptMeasurements(SingletonMeasurements(m))
		
		val result = outstream.toString()
		Assert.assertTrue(result.contains("123"))
		Assert.assertTrue(result.contains("a1"))
		Assert.assertTrue(result.contains("v1"))
	}
	
	@Test
	fun twoAttributes()
	{
		val m = Measurement(123)
		m.metadata["a1"] = "v1"
		m.metadata["a2"] = "v2"
		printer.acceptMeasurements(SingletonMeasurements(m))
		
		val result = outstream.toString()
		Assert.assertTrue(result.contains("123"))
		Assert.assertTrue(result.contains("a1"))
		Assert.assertTrue(result.contains("v1"))
		Assert.assertTrue(result.contains("a2"))
		Assert.assertTrue(result.contains("v2"))
	}
	
	@Before
	fun setup()
	{
		printer.setStream(printstream)
	}
	
	val outstream = ByteArrayOutputStream()
	val printstream = PrintStream(outstream)
	val printer = MeasurementsPrinterConfiguration().createPlugin()
}