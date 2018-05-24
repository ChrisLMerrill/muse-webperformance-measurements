package com.webperformance.muse.measurements.tests

import com.webperformance.muse.measurements.*
import org.junit.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.project.*
import org.musetest.core.step.*
import org.musetest.core.steptest.*
import org.musetest.core.suite.*
import org.musetest.core.test.*


class TestDurationProducerTests
{
	val producer = TestDurationProducer(TestDurationProducerConfiguration())
	val project = SimpleProject()
	val test = SteppedTest(StepConfiguration("type_of_step"))
	val suite = SimpleTestSuite()
	val context = DefaultSteppedTestExecutionContext(ProjectExecutionContext(project), test)
	val config = BasicTestConfiguration(test)
	
	@Test
	fun oneStepOneDuration()
	{
		producer.initialize(context)
		val now = System.currentTimeMillis()
	    producer.processEvent(createStartEvent(now), context)
		val later = now + 1000
		producer.processEvent(createEndEvent(later), context)
		
		val measurements = producer.getMeasurements().iterator()
		val measurement = measurements.next()
		Assert.assertNotNull(measurement)
		Assert.assertEquals(1000L, measurement.value)
		Assert.assertEquals(later, measurement.metadata[Measurement.META_TIMESTAMP])
		Assert.assertEquals("duration", measurement.metadata[Measurement.META_METRIC])
		Assert.assertEquals("test1", measurement.metadata[Measurement.META_SUBJECT])
		Assert.assertEquals("test", measurement.metadata[Measurement.META_SUBJECT_TYPE])
		
		Assert.assertFalse(measurements.hasNext())
	}
	
	private fun createStartEvent(start_time: Long) : MuseEvent
	{
		val event = StartTestEventType.create(test.id, "test1")
		event.timestamp = start_time
		return event
	}
	
	private fun createEndEvent(end_time: Long) : MuseEvent
	{
		val event = EndTestEventType.create()
		event.timestamp = end_time
		return event
	}
	
	@Before
	fun setup()
	{
		test.id = "test1"
		suite.add(test)
		config.withinContext(context)
	}
}