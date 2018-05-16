package com.webperformance.muse.measurements.consumers

import au.com.bytecode.opencsv.*
import com.webperformance.muse.measurements.*
import com.webperformance.muse.measurements.containers.*
import org.junit.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.plugins.*
import org.musetest.core.project.*
import org.musetest.core.resultstorage.*
import java.io.*
import java.nio.file.*

class MeasurementsToCsvFilesTests
{
	@Test
	fun oneSubjectOneMetric()
	{
		// send two measurement to CSV
		_plugin.acceptMeasurements(createMeasurements(1, "count", "subject1"))
		_plugin.acceptMeasurements(createMeasurements(2, "count", "subject1"))
		
		_plugin.closeFiles()
		
		checkFile("subject1.csv", false)
	}

	private fun createMeasurements(value: Long, metric: String, subject: String) : Measurements
	{
		val measurement1 = Measurement(value)
		measurement1.addMetadata(Measurement.META_METRIC, metric)
		val measurements = MeasurementsWithCommonMetadata()
		measurements.metadata.put(Measurement.META_SUBJECT, subject)
		measurements.addMeasurement(measurement1)
		return measurements
	}
	
	private fun createMeasurements(value1: Long, value2: Long, metric1: String, metric2: String, subject: String) : Measurements
	{
		val measurement1 = Measurement(value1)
		measurement1.addMetadata(Measurement.META_METRIC, metric1)

		val measurement2 = Measurement(value2)
		measurement2.addMetadata(Measurement.META_METRIC, metric2)

		val measurements = MeasurementsWithCommonMetadata()
		measurements.metadata.put(Measurement.META_SUBJECT, subject)
		measurements.addMeasurement(measurement1)
		measurements.addMeasurement(measurement2)
		return measurements
	}
	
	@Test
	fun oneSubjectTwoMetrics()
	{
		_plugin.acceptMeasurements(createMeasurements(1, 11, "count", "duration", "subject1"))
		_plugin.acceptMeasurements(createMeasurements(2, 22, "count", "duration", "subject1"))
		
		_plugin.closeFiles()
		
		checkFile("subject1.csv", true)
	}
	
	@Test
	fun twoSubjectsTwoMetrics()
	{
		val measurements1 = MultipleMeasurements(createMeasurements(1, 11, "count", "duration", "subject1"))
		measurements1.add(createMeasurements(1, 11, "count", "duration", "subject2"))
		_plugin.acceptMeasurements(measurements1)

		val measurements2 = MultipleMeasurements(createMeasurements(2, 22, "count", "duration", "subject1"))
		measurements2.add(createMeasurements(2, 22, "count", "duration", "subject2"))
		_plugin.acceptMeasurements(measurements2)
		
		_plugin.closeFiles()
		
		checkFile("subject1.csv", true)
		checkFile("subject2.csv", true)
	}
	
	@Test
	fun metricMissingFromOneMeasurements()
	{
		_plugin.acceptMeasurements(createMeasurements(1, 11, "count", "duration", "subject1"))
		_plugin.acceptMeasurements(createMeasurements(2, "count", "subject1"))
		_plugin.acceptMeasurements(createMeasurements(3, 33, "count", "duration", "subject1"))
		
		_plugin.closeFiles()
		
		val reader = CSVReader(FileReader(File(_location.baseFolder, "subject1.csv")))
		reader.readNext() // skip header
		
		val values1 = reader.readNext()
		Assert.assertEquals("0", values1[0])
		Assert.assertEquals("1", values1[1])
		Assert.assertEquals("11", values1[2])
		val values2 = reader.readNext()
		Assert.assertEquals("1", values2[0])
		Assert.assertEquals("2", values2[1])
		Assert.assertEquals("", values2[2])
		val values3 = reader.readNext()
		Assert.assertEquals("2", values3[0])
		Assert.assertEquals("3", values3[1])
		Assert.assertEquals("33", values3[2])
	}
	
	private fun checkFile(name: String, second: Boolean)
	{
		// check the file contents
		val file = File(_location.baseFolder, name)
		Assert.assertTrue(file.exists())
		val reader = CSVReader(FileReader(file))
		val headers = reader.readNext()
		Assert.assertEquals(Measurement.META_SEQUENCE, headers[0])
		Assert.assertEquals("count", headers[1])
		if (second)
			Assert.assertEquals("duration", headers[2])
		
		val values1 = reader.readNext()
		Assert.assertEquals("0", values1[0])
		Assert.assertEquals("1", values1[1])
		if (second)
			Assert.assertEquals("11", values1[2])
		
		val values2 = reader.readNext()
		Assert.assertEquals("1", values2[0])
		Assert.assertEquals("2", values2[1])
		if (second)
			Assert.assertEquals("22", values2[2])
	}
	
	@Before
	fun setup()
	{
		_context.addPlugin(_location)
		_context.addPlugin(_plugin)
		_context.initializePlugins()
	}
	
	@After
	fun teardown()
	{
		_location._folder.deleteOnExit()
	}
	
	val _project = SimpleProject()
	val _context = ProjectExecutionContext(_project)
	val _location = TestStorageProvider()
	val _plugin = MeasurementsToCsvFilesConfiguration().createPlugin()
	
	class TestStorageProvider : MusePlugin, LocalStorageLocationProvider
	{
		override fun conditionallyAddToContext(context: MuseExecutionContext, automatic: Boolean): Boolean
		{
			context.addPlugin(this)
			return true
		}
		
		override fun initialize(context: MuseExecutionContext?)
		{
		
		}
		
		override fun getBaseFolder(): File
		{
			return _folder
		}
		
		override fun getTestFolder(context: TestExecutionContext): File
		{
			return _folder
		}
		
		val _folder = Files.createTempDirectory("musetest").toFile()
	}
}