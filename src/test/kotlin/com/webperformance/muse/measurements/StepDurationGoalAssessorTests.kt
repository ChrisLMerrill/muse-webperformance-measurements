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
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], true)
	}
	
	private fun assertGoalAssessmentEventCount(count: Long)
	{
		var found = 0L
		for (event in events_received)
			when (event)
			{
				is GoalAssessmentEvent ->
				{
					found++
				}
			}
		Assert.assertEquals("Wrong number of GoalAssessmentEvents found", count, found)
	}
	
	@Test
	fun failGoalEvent()
	{
		// create an assessor
		runTest(1200L, 1000L)
		
		// check the received events
		Assert.assertEquals(3, events_received.size)
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], false)
		assertGoalEventMessageContains(events_received[2], "1000")
	}
	
	private fun assertGoalEventStatus(event: MuseEvent, satisfied: Boolean)
	{
		when (event)
		{
			is GoalAssessmentEvent ->
			{
				Assert.assertEquals(satisfied, event.goalSatisfied)
			}
			else ->
			{
				Assert.assertTrue("event is not a GoalAssessmentEvent", false)
			}
		}
		
	}
	
	private fun assertGoalEventMessageContains(event: MuseEvent, the_thing: String)
	{
		when (event)
		{
			is GoalAssessmentEvent ->
			{
				Assert.assertTrue("event message does not contain " + the_thing, event.message.contains(the_thing))
			}
			else ->
			{
				Assert.assertTrue("event is not a GoalAssessmentEvent", false)
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
		assertGoalAssessmentEventCount(1)
	}

	@Test
	fun ignoreUntaggedSteps()
	{
		val config = ContextInitializerConfiguration()
		config.addParameter("step-has-tag", ValueSourceConfiguration.forValue("assess-goal"))
		runTest(200L, 100L, config)
		
		// there should be no goal assessment event
		assertGoalAssessmentEventCount(0)
	}
	
	@Test
	fun evaluateTaggedSteps()
	{
		val config = ContextInitializerConfiguration()
		config.addParameter("step-has-tag", ValueSourceConfiguration.forValue("assess-goal"))
// TODO this should use a tags API
		step_config.setMetadataField("tags", "assess-goal")
		runTest(200L, 80L, config)
		
		// there should be 1 goal assessment event (a fail)
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], false)
		assertGoalEventMessageContains(events_received[2], "80")
	}
	
	private fun runTest(duration: Long, goal: Long)
	{
		runTest(duration, goal, ContextInitializerConfiguration())
	}

	private fun runTest(duration: Long, goal: Long, config: ContextInitializerConfiguration)
	{
		initialize(config, goal)
		assessor.initialize(context) // it should subscribe itself to the context
		runStep(duration, step_config)
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
	
	@Test
	fun useGoalConfiguredOnStep()
	{
		val config = ContextInitializerConfiguration()
		config.addParameter("step-goal-name", ValueSourceConfiguration.forValue("duration-goal"))
		step_config.setMetadataField("duration-goal", 100)
		runTest(300L, 500L, config)
		
		// there should be 1 goal assessment event (a fail)
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], false)
		assertGoalEventMessageContains(events_received[2], "100")
	}
	
	@Test
	fun assessGoalEvenWhenCustomGoalNotConfiguredOnStep()
	{
		/*
		 * If the assessor has been configured with a custom goal name, it should still evaluate
		 * the steps without a custom goal using the default goal.
		 */
		val config = ContextInitializerConfiguration()
		config.addParameter("step-goal-name", ValueSourceConfiguration.forValue("duration-goal"))
		runTest(300L, 500L, config)
		
		// there should be 1 goal assessment event (a fail)
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], true)
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