package com.webperformance.muse.measurements.consumers

import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.junit.*
import org.musetest.core.mocks.MockSteppedTestExecutionContext
import java.io.*

class MeasurementPrinterTests
{
	@Test
	fun plainValue()
	{
		val m = Measurement(123)
		printer.acceptMeasurements(MeasurementsWithCommonMetadata(m))
		
		val result = outstream.toString()
		Assert.assertTrue(result.contains("123"))
	}
	
	@Test
	fun oneAttribute()
	{
		val m = Measurement(123)
		m.metadata["a1"] = "v1"
		printer.acceptMeasurements(MeasurementsWithCommonMetadata(m))
		
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
		printer.acceptMeasurements(MeasurementsWithCommonMetadata(m))
		
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
        printer.initialize(MockSteppedTestExecutionContext())
	}
	
	val outstream = ByteArrayOutputStream()
	val printstream = PrintStream(outstream)
	val printer = MeasurementsPrinterConfiguration().createPlugin()
}