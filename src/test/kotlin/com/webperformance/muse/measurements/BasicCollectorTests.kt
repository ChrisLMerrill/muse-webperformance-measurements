package com.webperformance.muse.measurements

import org.junit.Assert
import org.junit.Test
import org.musetest.core.MuseEventType
import org.musetest.core.events.StepEvent
import org.musetest.core.step.StepConfiguration
import org.musetest.core.tests.mocks.MockStepEvent
import org.musetest.core.tests.mocks.MockStepExecutionContext
import java.io.ByteArrayOutputStream

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class BasicCollectorTests
{
	@Test
	fun collectSingleStepDuration()
	{
		val stepConfig = StepConfiguration("mock-step")
		stepConfig.setMetadataField(StepConfiguration.META_ID, 9L)
		val context = MockStepExecutionContext()
		val startEvent = StepEvent(MuseEventType.StartStep, stepConfig, context)
		val endEvent = MockStepEvent(MuseEventType.EndStep, stepConfig, context)
		endEvent.timestampNanos = startEvent.timestampNanos + 1000

		// create a collector
		val collector = StepDurationCollector()
		collector.initialize(context) // it should subscribe itself to the context
		context.raiseEvent(startEvent)
		context.raiseEvent(endEvent)
		
		val test_data = collector.data
		Assert.assertEquals(1, test_data.durations.size)  // collected for 1 step
		Assert.assertNotNull(test_data.durations.get(9L));
		Assert.assertEquals(1, test_data.durations.get(9L)?.size)  // collected 1 duration for that step
		Assert.assertNotNull(test_data.durations.get(9L)?.get(0));
		Assert.assertEquals(1000L, test_data.durations.get(9L)?.get(0))

		// Save the data and compare to the expected output
		val outstream = ByteArrayOutputStream()
		test_data.write(outstream)
		Assert.assertEquals(this.javaClass.getResource("compareto/durations.txt").readText(), outstream.toString())
	}
}