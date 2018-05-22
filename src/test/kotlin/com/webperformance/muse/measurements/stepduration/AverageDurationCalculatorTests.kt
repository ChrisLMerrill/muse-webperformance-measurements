package com.webperformance.muse.measurements.stepduration

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.musetest.core.MuseEvent
import org.musetest.core.events.EndStepEventType
import org.musetest.core.events.StartStepEventType
import org.musetest.core.mocks.MockSteppedTestExecutionContext
import org.musetest.core.step.BasicStepExecutionResult
import org.musetest.core.step.StepConfiguration
import org.musetest.core.step.StepExecutionStatus
import org.musetest.core.steptest.SteppedTest

class AverageDurationCalculatorTests
{
	@Test
	fun averageOfSingleDuration()
	{
		val calculator = AverageStepDurationCalculatorConfiguration.StepDurationCalculatorType().create().createPlugin()
		calculator.conditionallyAddToContext(context, false)
		context.initializePlugins()
		
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
		
		Assert.assertEquals(1, calculator.data.averages.size)
		Assert.assertEquals(1000L, calculator.data.averages[9L])
	}
	
	@Test
	fun averageOfTwoDurations()
	{
		val calculator = AverageStepDurationCalculatorConfiguration.StepDurationCalculatorType().create().createPlugin()
		calculator.conditionallyAddToContext(context, false)
		context.initializePlugins()
		
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

		createEvents(600L)
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
		
		Assert.assertEquals(1, calculator.data.averages.size)
		Assert.assertEquals(800L, calculator.data.averages[9L])
	}
	
	@Test
	fun averageForTwoSteps()
	{
		val step2 = StepConfiguration("step5")
		step2.setMetadataField(StepConfiguration.META_ID, 5L)
		step_config.addChild(step2)
		context = MockSteppedTestExecutionContext(test)
		
		val calculator = AverageStepDurationCalculatorConfiguration.StepDurationCalculatorType().create().createPlugin()
		calculator.conditionallyAddToContext(context, false)
		context.initializePlugins()
		
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

		createEvents(600L)
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)

		createEvents(300L, step2)
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
		
		Assert.assertEquals(2, calculator.data.averages.size)
		Assert.assertEquals(800L, calculator.data.averages[9L])
		Assert.assertEquals(300L, calculator.data.averages[5L])
	}
	
	@Before
	fun setup()
	{
		step_config = StepConfiguration("step9")
		step_config.setMetadataField(StepConfiguration.META_ID, 9L)
		test = SteppedTest(step_config)
		context = MockSteppedTestExecutionContext(test)
		createEvents(1000L)
	}
	
	private fun createEvents(duration: Long, step: StepConfiguration = step_config)
	{
		start_event = StartStepEventType.create(step, context)
		end_event = EndStepEventType.create(step, context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		end_event.timestamp = start_event.timestamp + duration
	}
	
	
	private lateinit var step_config : StepConfiguration
	private lateinit var start_event : MuseEvent
	private lateinit var end_event : MuseEvent
	private lateinit var context : MockSteppedTestExecutionContext
	private lateinit var test : SteppedTest
}