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
		Assert.assertEquals(1, test_data.durations.size.toLong())
		Assert.assertEquals(1000L, test_data.durations[0].value.toLong())

		// Save the data and compare to the expected output
		val outstream = ByteArrayOutputStream()
		test_data.write(outstream)
		Assert.assertEquals(this.javaClass.getResource("compareto/durations.txt").readText(), outstream.toString())
	}
}