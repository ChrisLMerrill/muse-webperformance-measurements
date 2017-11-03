package com.webperformance.muse.measurements

import com.webperformance.muse.measurements.stepduration.StepDurationGoalAssessor
import com.webperformance.muse.measurements.stepduration.StepDurationGoalCollector
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.musetest.core.MuseEvent
import org.musetest.core.context.ContextInitializer
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.events.StepEvent
import org.musetest.core.mocks.MockStepEvent
import org.musetest.core.mocks.MockStepExecutionContext
import org.musetest.core.step.BasicStepExecutionResult
import org.musetest.core.step.StepConfiguration
import org.musetest.core.step.StepExecutionStatus
import org.musetest.core.values.ValueSourceConfiguration

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationGoalsTests
{
	@Test
	fun failGoalCollected()
	{
		// create a goal collector
		val assessor = StepDurationGoalCollector()
		runTest(2000L, assessor, 1000L)
		
		// check the data
		Assert.assertEquals(0, assessor.passes(step_id))
		Assert.assertEquals(1, assessor.fails(step_id))
	}

	@Test
	fun passGoalCollected()
	{
		// create a collector
		val assessor = StepDurationGoalCollector()
		runTest(900L, assessor, 1000L)
		
		// check the data
		Assert.assertEquals(1, assessor.passes(step_config.stepId))
		Assert.assertEquals(0, assessor.fails(step_config.stepId))
	}
	
	@Test
	fun passGoalEvent()
	{
		// create an assessor
		val assessor = StepDurationGoalAssessor()
		runTest(900L, assessor, 1000L)
		
		// check the received events
		Assert.assertEquals(3, events_received.size)
		Assert.assertTrue(events_received[2] is GoalAssessmentEvent)
		val goal_event = events_received[2]
		when (goal_event)
		{
			is GoalAssessmentEvent ->
			{
				Assert.assertTrue(goal_event.goalSatisfied)
			}
		}
	}

	@Test
	fun failGoalEvent()
	{
		// create an assessor
		val assessor = StepDurationGoalAssessor()
		runTest(1200L, assessor, 1000L)
		
		// check the received events
		Assert.assertEquals(3, events_received.size)
		Assert.assertTrue(events_received[2] is GoalAssessmentEvent)
		val goal_event = events_received[2]
		when (goal_event)
		{
			is GoalAssessmentEvent ->
			{
				Assert.assertFalse(goal_event.goalSatisfied)
			}
		}
	}

	@Test
	fun ignoreIncompleteStepEndEvents()
	{
		// create an assessor
		val assessor = StepDurationGoalAssessor()
		runTest(1200L, assessor, 1000L)
		
		context.raiseEvent(StepEvent(StepEvent.START_INSTANCE, step_config, context))
		val incomplete_end_event = MockStepEvent(StepEvent.END_INSTANCE, step_config, context)
		incomplete_end_event.result = BasicStepExecutionResult(StepExecutionStatus.INCOMPLETE)
		incomplete_end_event.timestampNanos = start_event.timestampNanos + 5000L * 1000000L // 2 seconds in nanos
		context.raiseEvent(incomplete_end_event)
		
		// check the received events
		Assert.assertEquals(5, events_received.size)  // the incomplete_end_event should not trigger a goal event
	}

	private fun runTest(duration: Long, initializer: ContextInitializer, goal: Long)
	{
		val init_config = ContextInitializerConfiguration()
		init_config.addParameter("goal", ValueSourceConfiguration.forValue(goal))
		initializer.configure(init_config)
		initializer.initialize(context) // it should subscribe itself to the context
		
		start_event = StepEvent(StepEvent.START_INSTANCE, step_config, context)
		end_event = MockStepEvent(StepEvent.END_INSTANCE, step_config, context)
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