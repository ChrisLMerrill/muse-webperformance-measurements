package com.webperformance.muse.measurements.tests

import com.fasterxml.jackson.annotation.*
import org.musetest.core.*
import org.musetest.core.plugins.*
import org.musetest.core.resource.generic.*
import org.musetest.core.resource.types.*
import org.musetest.core.values.*
import org.musetest.core.values.descriptor.*

/**
 * Configuration for the TestDurationProducer. This is what the user creates in the UI and is used to
 * create the producer that does the work.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("test-duration-measurements-producer")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Running tests", description = "If this parameter is present and true, measure the number and duration of currently running tests", type = SubsourceDescriptor.Type.Named, name = TestDurationProducerConfiguration.COLLECT_RUNNING_PARAM, optional = true)
)
class TestDurationProducerConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return TestDurationProducerType()
	}

	override fun createPlugin(): TestDurationProducer
	{
		return TestDurationProducer(this)
	}


	@JsonIgnore
	fun isCollectRunningTests(context: MuseExecutionContext): Boolean
	{
		return isParameterTrue(context, COLLECT_RUNNING_PARAM)
	}
	
	class TestDurationProducerType : ResourceSubtype(TYPE_ID, "Test Duration Producer", TestDurationProducerConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): TestDurationProducerConfiguration
		{
			val config = TestDurationProducerConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Collects measurements of test duration")
		}
	}

	companion object
	{
		val TYPE_ID = TestDurationProducerConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val COLLECT_RUNNING_PARAM = "collect-running"
	}
	
}
