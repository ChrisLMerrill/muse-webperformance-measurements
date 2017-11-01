package com.webperformance.muse.measurements

import com.webperformance.muse.measurements.stepduration.StepDurationGoalAssessor
import org.junit.Assert
import org.junit.Test
import org.musetest.core.MuseEvent
import org.musetest.core.MuseEventType
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.events.StepEvent
import org.musetest.core.step.StepConfiguration
import org.musetest.core.tests.mocks.MockStepEvent
import org.musetest.core.tests.mocks.MockStepExecutionContext
import org.musetest.core.values.ValueSourceConfiguration

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationGoalAssessmentTests
{
	@Test
	fun collectSingleStepDuration()
	{
		val step_config = StepConfiguration("mock-step")
		step_config.setMetadataField(StepConfiguration.META_ID, 9L)
		val events_received = mutableListOf<MuseEvent>()
		val context = MockStepExecutionContext()
		val start_event = StepEvent(MuseEventType.StartStep, step_config, context)
		val end_event = MockStepEvent(MuseEventType.EndStep, step_config, context)
		end_event.timestampNanos = start_event.timestampNanos + 2000

		// create a assessor
		val assessor = StepDurationGoalAssessor()
		val init_config = ContextInitializerConfiguration()
		init_config.addParameter("goal", ValueSourceConfiguration.forValue(1000L))
		assessor.configure(init_config)
		assessor.initialize(context) // it should subscribe itself to the context
		
		// subscribe to events
		context.addEventListener { event -> events_received.add(event) }
		
		// send it Step events
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
		
		// check the data
		Assert.assertEquals(0, assessor.passes(step_config.stepId))
		Assert.assertEquals(1, assessor.fails(step_config.stepId))
		
		// check the received events
		Assert.assertTrue(events_received[0] == start_event)
		Assert.assertTrue(events_received[1] == end_event)
// TODO can't do this until EventType becomes a type class instead of an enum
//		Assert.assertTrue(events_received[2] is GoalAssessmentEvent)
//		GoalAssessmentEvent
	}
}