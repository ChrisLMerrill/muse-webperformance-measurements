package com.webperformance.muse.measurements

import org.junit.*
import org.musetest.core.*
import org.musetest.core.mocks.*

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class ContextInitializerTests
{
	@Test
	@Throws(MuseExecutionError::class)
	fun injectStepDurationCollector()
	{
		val context = MockMuseExecutionContext()
		context.addInitializer(DataCollectionInitializerConfiguration().createInitializer())
		context.runInitializers()

		Assert.assertEquals(1, context.listeners.size.toLong())
		Assert.assertTrue(context.listeners[0] is StepDurationCollector)
	}
}


