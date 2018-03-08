package com.webperformance.muse.measurements.steps

import com.webperformance.muse.measurements.containers.*
import org.junit.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.project.*
import org.musetest.core.step.*
import org.musetest.core.steptest.*
import org.musetest.core.suite.*

class AllStepsAverageDurationMeasurementProducerTests
{
	@Test
	fun zeroDurations()
	{
		Assert.assertTrue(producer.getMeasurements() is EmptyMeasurements)
	}
	
	@Test
	fun averageOfSingleDuration()
	{
		producer.processEvent(start_event, step_config)
		producer.processEvent(end_event, step_config)
		
		val measurement = producer.getMeasurements().iterator().next()
		
		Assert.assertEquals(1000L, measurement.value)
	}
	
	@Test
	fun averageOfTwoDurations()
	{
		producer.processEvent(start_event, step_config)
		producer.processEvent(end_event, step_config)

		createEvents(600L)
		producer.processEvent(start_event, step_config)
		producer.processEvent(end_event, step_config)

		val measurement = producer.getMeasurements().iterator().next()
		
		Assert.assertEquals(800L, measurement.value)
	}
	
	@Test
	fun twoMeasurements()
	{
		producer.processEvent(start_event, step_config)
		producer.processEvent(end_event, step_config)

		val measurement1 = producer.getMeasurements().iterator().next()
		Assert.assertEquals(1000L, measurement1.value)
		
		createEvents(600L)
		producer.processEvent(start_event, step_config)
		producer.processEvent(end_event, step_config)

		val measurement2 = producer.getMeasurements().iterator().next()
		Assert.assertEquals(600L, measurement2.value)
	}
	
	@Before
	fun setup()
	{
		step_config = StepConfiguration("step9")
		step_config.addTag("measure")
		step_config.setMetadataField(StepConfiguration.META_ID, 9L)

		test = SteppedTest(step_config)
		val context = DefaultSteppedTestExecutionContext(DefaultTestSuiteExecutionContext(SimpleProject(), SimpleTestSuite()), test)
		step_context = SingleStepExecutionContext(context, step_config, false)

		createEvents(1000L)

		producer = AllStepsAverageDurationMeasurementProducer()
	}
	
	private fun createEvents(duration: Long, step: StepConfiguration = step_config)
	{
		start_event = StartStepEventType.create(step, step_context)
		end_event = EndStepEventType.create(step, step_context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		end_event.timestampNanos = start_event.timestampNanos + (duration * 1000000)
	}
	
	
	private lateinit var step_config : StepConfiguration
	private lateinit var start_event : MuseEvent
	private lateinit var end_event : MuseEvent
	private lateinit var test : SteppedTest
	private lateinit var step_context : StepExecutionContext
	private lateinit var producer : AllStepsAverageDurationMeasurementProducer
}