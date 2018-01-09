package com.webperformance.muse.measurements.stepduration

import com.webperformance.muse.measurements.GoalAssessmentEventType
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
import org.musetest.core.test.plugins.TestPluginConfiguration
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
			if (event.typeId.equals(GoalAssessmentEventType.TYPE_ID))
				found++
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
		if (event.typeId.equals(GoalAssessmentEventType.TYPE_ID))
			Assert.assertEquals(satisfied, !event.hasTag(MuseEvent.FAILURE))
		else
			Assert.assertTrue("event is not a GoalAssessmentEvent", false)
	}
	
	private fun assertGoalEventMessageContains(event: MuseEvent, the_thing: String)
	{
		if (event.typeId.equals(GoalAssessmentEventType.TYPE_ID))
			Assert.assertTrue("event message does not contain " + the_thing, GoalAssessmentEventType().getDescription(event).contains(the_thing))
		else
			Assert.assertTrue("event is not a GoalAssessmentEvent", false)
	}
	
	@Test
	fun ignoreIncompleteStepEndEvents()
	{
		// create an assessor
		runTest(1200L, 1000L)
		
		context.raiseEvent(StartStepEventType.create(step_config, context))
		val incomplete_end_event = EndStepEventType.create(step_config, context, BasicStepExecutionResult(StepExecutionStatus.INCOMPLETE))
		incomplete_end_event.timestampNanos = start_event.timestampNanos + 5000L * 1000000L // 2 seconds in nanos
		context.raiseEvent(incomplete_end_event)
		
		// check the received events
		Assert.assertEquals(5, events_received.size)  // the incomplete_end_event should not trigger a goal event
		assertGoalAssessmentEventCount(1)
	}

	@Test
	fun ignoreUntaggedSteps()
	{
		val config = TestPluginConfiguration()
		config.addParameter("step-has-tag", ValueSourceConfiguration.forValue("assess-goal"))
		runTest(200L, 100L, config)
		
		// there should be no goal assessment event
		assertGoalAssessmentEventCount(0)
	}
	
	@Test
	fun evaluateTaggedSteps()
	{
		val config = TestPluginConfiguration()
		config.addParameter("step-has-tag", ValueSourceConfiguration.forValue("assess-goal"))
		step_config.addTag("assess-goal")
		runTest(200L, 80L, config)
		
		// there should be 1 goal assessment event (a fail)
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], false)
		assertGoalEventMessageContains(events_received[2], "80")
	}
	
	private fun runTest(duration: Long, goal: Long)
	{
		runTest(duration, goal, TestPluginConfiguration())
	}

	private fun runTest(duration: Long, goal: Long, config: TestPluginConfiguration)
	{
		initialize(config, goal)
		assessor.initialize(context) // it should subscribe itself to the context
		runStep(duration)
	}

	private fun initialize(config: TestPluginConfiguration, goal: Long)
	{
		config.addParameter("goal", ValueSourceConfiguration.forValue(goal))
		assessor.configure(config)
	}
	
	private fun runStep(duration: Long)
	{
		end_event.timestampNanos = start_event.timestampNanos + duration * 1000000 // 2 seconds in nanos
		
		// send it Step events
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
	}
	
	@Test
	fun useGoalConfiguredOnStep()
	{
		val config = TestPluginConfiguration()
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
		val config = TestPluginConfiguration()
		config.addParameter("step-goal-name", ValueSourceConfiguration.forValue("duration-goal"))
		runTest(300L, 500L, config)
		
		// there should be 1 goal assessment event (a fail)
		assertGoalAssessmentEventCount(1)
		assertGoalEventStatus(events_received[2], true)
	}
	
	@Before
	fun setup()
	{
		step_config = StepConfiguration("mock-step")
		step_config.setMetadataField(StepConfiguration.META_ID, step_id)
		step_test = SteppedTest(step_config)
		step_test.id = "stepper"
		context = MockSteppedTestExecutionContext(step_test)
		start_event = StartStepEventType.create(step_config, context)
		end_event = EndStepEventType.create(step_config, context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		
		// subscribe to events
		context.addEventListener { event -> events_received.add(event) }
	}
	
	private val step_id = 9L
	private val events_received = mutableListOf<MuseEvent>()
	private lateinit var step_config : StepConfiguration
	private lateinit var step_test : SteppedTest
	private lateinit var context : MockSteppedTestExecutionContext
	private lateinit var start_event : MuseEvent
	private lateinit var end_event : MuseEvent
	private val assessor = StepDurationGoalAssessor()
}