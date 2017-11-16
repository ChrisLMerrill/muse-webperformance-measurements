package com.webperformance.muse.measurements

import com.webperformance.muse.measurements.stepduration.StepDurationGoalAssessor
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.musetest.core.MuseEvent
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
class StepDurationGoalAssessorTests
{
	@Test
	fun passGoalEvent()
	{
		// create an assessor
		runTest(900L, 1000L)
		
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
		runTest(1200L, 1000L)
		
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
		runTest(1200L, 1000L)
		
		context.raiseEvent(StepEvent(StepEvent.START_INSTANCE, step_config, context))
		val incomplete_end_event = MockStepEvent(StepEvent.END_INSTANCE, step_config, context)
		incomplete_end_event.result = BasicStepExecutionResult(StepExecutionStatus.INCOMPLETE)
		incomplete_end_event.timestampNanos = start_event.timestampNanos + 5000L * 1000000L // 2 seconds in nanos
		context.raiseEvent(incomplete_end_event)
		
		// check the received events
		Assert.assertEquals(5, events_received.size)  // the incomplete_end_event should not trigger a goal event
	}

	private fun runTest(duration: Long, goal: Long)
	{
		initialize(goal)
		assessor.initialize(context) // it should subscribe itself to the context
		runStep(duration, step_config)
	}

	private fun initialize(goal: Long)
	{
		val config = ContextInitializerConfiguration()
		initialize(config, goal)
	}
	private fun initialize(config: ContextInitializerConfiguration, goal: Long)
	{
		config.addParameter("goal", ValueSourceConfiguration.forValue(goal))
		assessor = StepDurationGoalAssessor()
		assessor.configure(config)
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
	private var assessor = StepDurationGoalAssessor()
}