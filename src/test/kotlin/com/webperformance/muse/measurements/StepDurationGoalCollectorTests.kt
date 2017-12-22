package com.webperformance.muse.measurements

import com.webperformance.muse.measurements.stepduration.StepDurationGoalCollector
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.musetest.core.MuseEvent
import org.musetest.core.events.StepEvent
import org.musetest.core.mocks.MockStepEvent
import org.musetest.core.mocks.MockStepExecutionContext
import org.musetest.core.step.BasicStepExecutionResult
import org.musetest.core.step.StepConfiguration
import org.musetest.core.step.StepExecutionStatus
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
		val collector = StepDurationGoalCollector()
		runTest(2000L, collector, 1000L)
		
		// check the data
		Assert.assertEquals(0, collector.passes(step_id))
		Assert.assertEquals(1, collector.fails(step_id))
	}

	@Test
	fun passGoalCollected()
	{
		// create a collector
		val collector = StepDurationGoalCollector()
		runTest(900L, collector, 1000L)
		
		// check the data
		Assert.assertEquals(1, collector.passes(step_config.stepId))
		Assert.assertEquals(0, collector.fails(step_config.stepId))
	}
	
	private fun runTest(duration: Long, initializer: TestPlugin, goal: Long)
	{
		val config = TestPluginConfiguration()
		config.addParameter("goal", ValueSourceConfiguration.forValue(goal))
		initializer.configure(config)
		initializer.initialize(context) // it should subscribe itself to the context
		runStep(duration, step_config)
	}

	private fun runStep(duration: Long, step: StepConfiguration)
	{
		start_event = StepEvent(StepEvent.START_INSTANCE, step, context)
		end_event = MockStepEvent(StepEvent.END_INSTANCE, step, context)
		end_event.result = BasicStepExecutionResult(StepExecutionStatus.COMPLETE)
		end_event.timestampNanos = start_event.timestampNanos + duration * 1000000 // 2 seconds in nanos
		
		// send it Step events
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
	}
	
	@Before
	fun setup()
	{
		step_id = 9L
		step_config = StepConfiguration("mock-step")
		step_config.setMetadataField(StepConfiguration.META_ID, step_id)
		events_received = mutableListOf()
		context = MockStepExecutionContext()

		// subscribe to events
		context.addEventListener { event -> events_received.add(event) }
	}
	
	private var step_id = 9L
	private var events_received = mutableListOf<MuseEvent>()
	private var context = MockStepExecutionContext()
	private var step_config = StepConfiguration("mock-step")
	private var start_event = StepEvent(StepEvent.START_INSTANCE, step_config, context)
	private var end_event = MockStepEvent(StepEvent.END_INSTANCE, step_config, context)
}