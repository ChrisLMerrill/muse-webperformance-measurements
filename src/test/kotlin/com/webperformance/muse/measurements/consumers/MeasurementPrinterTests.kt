package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.Measurement
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MeasurementPrinterTests
{
	@Test
	fun plainValue()
	{
		val m = Measurement(123)
		printer.acceptMeasurement(m)
		
		val result = outstream.toString()
		Assert.assertTrue(result.contains("123"))
	}
	
	@Test
	fun oneAttribute()
	{
		val m = Measurement(123)
		m.metadata["a1"] = "v1"
		printer.acceptMeasurement(m)
		
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
		printer.acceptMeasurement(m)
		
		val result = outstream.toString()
		Assert.assertTrue(result.contains("123"))
		Assert.assertTrue(result.contains("a1"))
		Assert.assertTrue(result.contains("v1"))
		Assert.assertTrue(result.contains("a2"))
		Assert.assertTrue(result.contains("v2"))
	}
	
	val outstream = ByteArrayOutputStream()
	val printstream = PrintStream(outstream)
	val printer = MeasurementPrinter(printstream)
}