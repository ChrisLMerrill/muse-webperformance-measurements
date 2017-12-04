package com.webperformance.muse.measurements

import com.webperformance.muse.measurements.stepduration.StepDurationCollector
import org.junit.Assert
import org.junit.Test
import org.musetest.core.events.StepEvent
import org.musetest.core.mocks.MockStepEvent
import org.musetest.core.mocks.MockStepExecutionContext
import org.musetest.core.step.StepConfiguration
import java.io.ByteArrayOutputStream

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class BasicCollectorTests
{
	@Test
	fun collectSingleStepDuration()
	{
		val step_config = StepConfiguration("mock-step")
		step_config.setMetadataField(StepConfiguration.META_ID, 9L)
		val context = MockStepExecutionContext()
		val start_event = StepEvent(StepEvent.START_INSTANCE, step_config, context)
		val end_event = MockStepEvent(StepEvent.END_INSTANCE, step_config, context)
		end_event.timestampNanos = start_event.timestampNanos + (1000 * 1000000)

		// create a collector
		val collector = StepDurationCollector()
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
		Assert.assertEquals(this.javaClass.getResource("compareto/durations.txt").readText(), outstream.toString())
	}
}