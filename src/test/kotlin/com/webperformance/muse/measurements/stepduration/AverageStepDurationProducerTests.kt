package com.webperformance.muse.measurements.stepduration

import org.junit.*
import org.musetest.core.*
import org.musetest.core.context.*
import org.musetest.core.events.*
import org.musetest.core.project.*
import org.musetest.core.step.*
import org.musetest.core.steptest.*
import org.musetest.core.suite.*

class AverageStepDurationProducerTests
{
	@Test
	fun averageOfSingleDuration()
	{
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
		
		val measurement = producer.getMeasurements().iterator().next()
		
		Assert.assertEquals(1000L, measurement.value)
	}
	
	@Test
	fun averageOfTwoDurations()
	{
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

		createEvents(600L)
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

		val measurement = producer.getMeasurements().iterator().next()
		
		Assert.assertEquals(800L, measurement.value)
	}
	
	@Test
	fun twoMeasurements()
	{
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

		val measurement1 = producer.getMeasurements().iterator().next()
		Assert.assertEquals(1000L, measurement1.value)
		
		createEvents(600L)
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

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
		val suite_context = DefaultTestSuiteExecutionContext(SimpleProject(), SimpleTestSuite())
		context = DefaultSteppedTestExecutionContext(suite_context, test)
		createEvents(1000L)

		producer = AverageStepDurationProducerConfiguration.AverageStepDurationProducerType().create().createPlugin()
		producer.conditionallyAddToContext(suite_context, false)
		suite_context.initializePlugins()
		context.addPlugin(producer)
		context.initializePlugins()
	}
	
	private fun createEvents(duration: Long, step: StepConfiguration = step_config)
	{
		val step_context = SingleStepExecutionContext(context, step, false)
		start_event = StartStepEventType.create(step, step_context)
		end_event = EndStepEventType.create(step, step_context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		end_event.timestampNanos = start_event.timestampNanos + (duration * 1000000)
	}
	
	
	private lateinit var step_config : StepConfiguration
	private lateinit var start_event : MuseEvent
	private lateinit var end_event : MuseEvent
	private lateinit var context : DefaultSteppedTestExecutionContext
	private lateinit var test : SteppedTest
	private lateinit var producer : AverageStepDurationProducer
}
