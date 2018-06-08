package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import org.junit.*
import org.musetest.builtins.step.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.project.*
import org.musetest.core.step.*
import org.musetest.core.suite.*
import org.musetest.core.values.*

class StepSummaryMeasurementsProducerTests
{
	val step1 = StepConfiguration(LogMessage.TYPE_ID)
	val step2 = StepConfiguration(LogMessage.TYPE_ID)
	
	@Test
	fun testSuccess()
	{
		val configuration = StepSummaryMeasurementsProducerConfiguration()
		configuration.parameters().addSource(StepSummaryMeasurementsProducerConfiguration.COLLECT_SUCCESS_STEPS_PARAM, ValueSourceConfiguration.forValue(true))
		configuration.parameters().addSource(StepSummaryMeasurementsProducerConfiguration.COLLECT_FAILED_STEPS_PARAM, ValueSourceConfiguration.forValue(true))
		configuration.parameters().addSource(StepSummaryMeasurementsProducerConfiguration.COLLECT_ERRORED_STEPS_PARAM, ValueSourceConfiguration.forValue(true))
		val producer = StepSummaryMeasurementsProducer(configuration)
		producer.initialize(DefaultTestSuiteExecutionContext(SimpleProject(), SimpleTestSuite()))
		val now = System.currentTimeMillis()
	    producer.processEvent(createStartEvent(step1, now), step1, "test1")
		val later = now + 1000
		producer.processEvent(createEndEvent(step1, later, true), step1, "test1")
		
		val measurements = mutableListOf<Measurement>()
		for (measurement in producer.getMeasurements().iterator())
			measurements.add(measurement)
		
		val successes = findMeasurement(measurements, StepSummaryMeasurementsProducer.SUCCESS_COUNT)
		Assert.assertNotNull(successes)
		Assert.assertEquals(1, successes!!.value)
		
		val failures = findMeasurement(measurements, StepSummaryMeasurementsProducer.FAILURE_COUNT)
		Assert.assertNotNull(failures)
		Assert.assertEquals(0, failures?.value)
		
		val errors = findMeasurement(measurements, StepSummaryMeasurementsProducer.ERROR_COUNT)
		Assert.assertNotNull(errors)
		Assert.assertEquals(0, errors?.value)
	}
	
	@Test
	fun testRunning()
	{
		val configuration = StepSummaryMeasurementsProducerConfiguration()
		configuration.parameters().addSource(StepSummaryMeasurementsProducerConfiguration.COLLECT_RUNNING_PARAM, ValueSourceConfiguration.forValue(true))
		configuration.parameters().addSource(StepSummaryMeasurementsProducerConfiguration.ADD_TEST_ID_PARAM, ValueSourceConfiguration.forValue(true))
		val producer = StepSummaryMeasurementsProducer(configuration)
		producer.initialize(DefaultTestSuiteExecutionContext(SimpleProject(), SimpleTestSuite()))
		val now = System.currentTimeMillis()
	    producer.processEvent(createStartEvent(step1, now), step1, "test1")
		
		val measurements = mutableListOf<Measurement>()
		for (measurement in producer.getMeasurements().iterator())
			measurements.add(measurement)
		
		val running = findMeasurement(measurements, StepSummaryMeasurementsProducer.RUNNING_COUNT)
		Assert.assertNotNull(running)
		Assert.assertEquals(1, running?.value)
		Assert.assertEquals("test1", running!!.metadata[Measurement.META_TEST])
	}
	
	private fun findMeasurement(list: List<Measurement>, metric: String): Measurement?
	{
		for (measurement in list)
			if (measurement.metadata[Measurement.META_METRIC] == metric)
				return measurement
		
		return null
	}
	
	private fun createStartEvent(config: StepConfiguration, start_time: Long) : MuseEvent
	{
		val event = StepEventType.create(StartStepEventType.TYPE_ID, config)
		event.timestamp = start_time
		return event
	}
	
	private fun createEndEvent(config: StepConfiguration, start_time: Long, success: Boolean) : MuseEvent
	{
		val event = StepEventType.create(EndStepEventType.TYPE_ID, config)
		event.timestamp = start_time
		if (!success)
			event.addTag(MuseEvent.FAILURE)
		return event
	}
	
	@Before
	fun setup()
	{
	    step1.stepId = 1L
	    step2.stepId = 2L
	}
}