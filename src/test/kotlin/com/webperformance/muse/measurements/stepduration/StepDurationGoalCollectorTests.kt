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
import org.musetest.core.test.plugins.TestPlugin
import org.musetest.core.test.plugins.TestPluginConfiguration
import org.musetest.core.values.ValueSourceConfiguration

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationGoalCollectorTests
{
	@Test
	fun failGoalCollected()
	{
		// create a goal collector
		val collector = StepDurationGoalAssessor()
		runTest(2000L, collector, 1000L)
		
		// check the data
		Assert.assertEquals(0L, collector.data?.getPasses(step_id))
		Assert.assertEquals(1L, collector.data?.getFails(step_id))
	}

	@Test
	fun passGoalCollected()
	{
		// create a collector
		val collector = StepDurationGoalAssessor()
		runTest(900L, collector, 1000L)
		
		// check the data
		Assert.assertEquals(1L, collector.data?.getPasses(step_id))
		Assert.assertEquals(0L, collector.data?.getFails(step_id))
	}
	
	private fun runTest(duration: Long, initializer: TestPlugin, goal: Long)
	{
		val config = TestPluginConfiguration()
		config.addParameter("goal", ValueSourceConfiguration.forValue(goal))
		config.addParameter("collect-goals", ValueSourceConfiguration.forValue(true))
		initializer.configure(config)
		initializer.initialize(context) // it should subscribe itself to the context
		runStep(duration)
	}

	private fun runStep(duration: Long)
	{
		end_event.timestampNanos = start_event.timestampNanos + duration * 1000000 // 2 seconds in nanos
		
		// send it Step events
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
	}
	
	@Before
	fun setup()
	{
		step_config = StepConfiguration("mock-step")
		step_config.setMetadataField(StepConfiguration.META_ID, step_id)
		step_test = SteppedTest(step_config)
		context = MockSteppedTestExecutionContext(step_test)

		start_event = StartStepEventType.create(step_config, context)
		end_event = EndStepEventType.create(step_config, context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		
		// subscribe to events
		context.addEventListener { event -> events_received.add(event) }
	}
	
	private var step_id = 9L
	private var events_received = mutableListOf<MuseEvent>()
	private lateinit var step_config : StepConfiguration
	private lateinit var step_test : SteppedTest
	private lateinit var context : MockSteppedTestExecutionContext
	private lateinit var start_event : MuseEvent
	private lateinit var end_event : MuseEvent
}