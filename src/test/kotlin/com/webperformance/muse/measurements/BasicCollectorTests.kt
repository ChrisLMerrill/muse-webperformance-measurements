package com.webperformance.muse.measurements

import org.junit.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.step.*
import org.musetest.core.tests.mocks.*

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

		Assert.assertEquals(1, collector.measurements.size.toLong())
		Assert.assertEquals(1000L, collector.measurements[0].value.toLong())
		}
	}