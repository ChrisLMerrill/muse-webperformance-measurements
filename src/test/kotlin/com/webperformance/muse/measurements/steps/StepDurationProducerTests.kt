package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.*
import org.junit.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.step.*

/*
 * Much of the work is done in the calculator inside the producer, which has its own set of unit tests, so
 * these tests simply ensure it is wired up.
 */
class StepDurationProducerTests
{
	val producer = StepDurationProducer(StepDurationProducerConfiguration())
	val step1 = StepConfiguration()
	val step2 = StepConfiguration()
	
	@Test
	fun oneStepOneDuration()
	{
		val now = System.currentTimeMillis()
	    producer.processEvent(createStartEvent(step1, now), step1, "id1")
		val later = now + 1000
		producer.processEvent(createEndEvent(step1, later), step1, "id1")
		
		val measurements = producer.getMeasurements().iterator()
		val measurement = measurements.next()
		Assert.assertNotNull(measurement)
		Assert.assertEquals(1000L, measurement.value)
		Assert.assertEquals(later, measurement.metadata[Measurement.META_TIMESTAMP])
		Assert.assertEquals("step.duration", measurement.metadata[Measurement.META_METRIC])
		Assert.assertEquals("step:1", measurement.metadata[Measurement.META_SUBJECT])
		
		Assert.assertFalse(measurements.hasNext())
	}
	
	private fun createStartEvent(config: StepConfiguration, start_time: Long) : MuseEvent
	{
		val event = StepEventType.create(StartStepEventType.TYPE_ID, config)
		event.timestamp = start_time
		return event
	}
	
	private fun createEndEvent(config: StepConfiguration, start_time: Long) : MuseEvent
	{
		val event = StepEventType.create(EndStepEventType.TYPE_ID, config)
		event.timestamp = start_time
		return event
	}
	
	@Before
	fun setup()
	{
	    step1.stepId = 1L
	    step2.stepId = 2L
	}
}