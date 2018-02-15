package com.webperformance.muse.measurements.stepduration

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.musetest.core.MuseEvent
import org.musetest.core.events.EndStepEventType
import org.musetest.core.events.StartStepEventType
import org.musetest.core.mocks.MockSteppedTestExecutionContext
import org.musetest.core.project.SimpleProject
import org.musetest.core.step.BasicStepExecutionResult
import org.musetest.core.step.StepConfiguration
import org.musetest.core.step.StepExecutionStatus
import org.musetest.core.steptest.SteppedTest
import org.musetest.core.suite.DefaultTestSuiteExecutionContext
import org.musetest.core.suite.SimpleTestSuite

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
		context = MockSteppedTestExecutionContext(test)
		createEvents(1000L)

		producer = AverageStepDurationProducerConfiguration.AverageStepDurationProducerType().create().createPlugin()
		val suite_context = DefaultTestSuiteExecutionContext(SimpleProject(), SimpleTestSuite())
		producer.conditionallyAddToContext(suite_context, false)
		suite_context.initializePlugins()
		producer.conditionallyAddToContext(context, false)
		context.initializePlugins()
	}
	
	private fun createEvents(duration: Long, step: StepConfiguration = step_config)
	{
		start_event = StartStepEventType.create(step, context)
		end_event = EndStepEventType.create(step, context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		end_event.timestampNanos = start_event.timestampNanos + (duration * 1000000)
	}
	
	
	private lateinit var step_config : StepConfiguration
	private lateinit var start_event : MuseEvent
	private lateinit var end_event : MuseEvent
	private lateinit var context : MockSteppedTestExecutionContext
	private lateinit var test : SteppedTest
	private lateinit var producer : AverageStepDurationProducer
}
