package com.webperformance.muse.measurements.stepduration

import org.junit.Assert
import org.junit.Test
import org.musetest.core.events.EndStepEventType
import org.musetest.core.events.StartStepEventType
import org.musetest.core.mocks.MockSteppedTestExecutionContext
import org.musetest.core.step.BasicStepExecutionResult
import org.musetest.core.step.StepConfiguration
import org.musetest.core.step.StepExecutionStatus
import org.musetest.core.steptest.SteppedTest
import java.io.ByteArrayOutputStream

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class StepDurationCollectorTests
{
	@Test
	fun collectSingleStepDuration()
	{
		val step_config = StepConfiguration("mock-step")
		step_config.setMetadataField(StepConfiguration.META_ID, 9L)
		val step_test = SteppedTest(step_config)
		val context = MockSteppedTestExecutionContext(step_test)
		val start_event = StartStepEventType.create(step_config, context)
		val end_event = EndStepEventType.create(step_config, context, BasicStepExecutionResult(StepExecutionStatus.COMPLETE))
		end_event.timestampNanos = start_event.timestampNanos + (1000 * 1000000)

		// create a collector
		val collector = StepDurationCollector(StepDurationCollectorConfiguration())
		collector.initialize(context) // it should subscribe itself to the context
		
		// send it Step events
		context.raiseEvent(start_event)
		context.raiseEvent(end_event)
		
		// check the collected data
		val test_data = collector.data
		Assert.assertEquals(1, test_data.durations.size)  // collected for 1 step
		Assert.assertNotNull(test_data.durations[9L])
		Assert.assertEquals(1, test_data.durations[9L]?.size)  // collected 1 duration for that step
		Assert.assertNotNull(test_data.durations[9L]?.get(0))
		Assert.assertEquals(1000L, test_data.durations[9L]?.get(0))

		// Save the data and compare to the expected output
		val outstream = ByteArrayOutputStream()
		test_data.write(outstream)
		Assert.assertEquals(this.javaClass.getResource("durations.txt").readText(), outstream.toString())
	}
}