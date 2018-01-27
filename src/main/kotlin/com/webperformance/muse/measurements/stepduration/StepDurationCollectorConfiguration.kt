package com.webperformance.muse.measurements.stepduration

import org.musetest.core.*
import org.musetest.core.resource.generic.*
import org.musetest.core.resource.types.*
import org.musetest.core.test.plugin.*
import org.musetest.core.values.*
import org.musetest.core.values.descriptor.*

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("step-duration-collector")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = BaseTestPlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = BaseTestPlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Step tag", description = "If this parameter is present, only collect durations on steps tagged with the value of this parameter", type = SubsourceDescriptor.Type.Named, name = StepDurationCollectorConfiguration.STEP_TAG_PARAM, optional = true)
)
class StepDurationCollectorConfiguration : GenericResourceConfiguration(), TestPluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return StepDurationCollectorType()
	}

	override fun createPlugin(): StepDurationCollector
	{
		return StepDurationCollector(this)
	}

	class StepDurationCollectorType : ResourceSubtype(TYPE_ID, "Step Duration Collector", StepDurationCollectorConfiguration::class.java, TestPluginConfiguration.TestPluginConfigurationResourceType())
	{

		override fun create(): StepDurationCollectorConfiguration
		{
			val config = StepDurationCollectorConfiguration()
			config.parameters().addSource(BaseTestPlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(BaseTestPlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(STEP_TAG_PARAM, ValueSourceConfiguration.forValue("list1"))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Measures and collects the durations of executed steps")
		}
	}

	companion object
	{

		val TYPE_ID = StepDurationCollectorConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val STEP_TAG_PARAM = "steptag"
	}
}


