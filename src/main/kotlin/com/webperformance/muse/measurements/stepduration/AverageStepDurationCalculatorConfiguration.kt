package com.webperformance.muse.measurements.stepduration

import org.musetest.core.MuseTypeId
import org.musetest.core.plugins.GenericConfigurablePlugin
import org.musetest.core.plugins.PluginConfiguration
import org.musetest.core.resource.generic.DefaultResourceDescriptor
import org.musetest.core.resource.generic.GenericResourceConfiguration
import org.musetest.core.resource.generic.ResourceDescriptor
import org.musetest.core.resource.types.ResourceSubtype
import org.musetest.core.resource.types.ResourceType
import org.musetest.core.values.ValueSourceConfiguration
import org.musetest.core.values.descriptor.MuseSubsourceDescriptor
import org.musetest.core.values.descriptor.MuseSubsourceDescriptors
import org.musetest.core.values.descriptor.SubsourceDescriptor

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("average-step-duration-calculator")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Step tag", description = "If this parameter is present, only collect durations on steps tagged with the value of this parameter", type = SubsourceDescriptor.Type.Named, name = StepDurationCollectorConfiguration.STEP_TAG_PARAM, optional = true)
)
class AverageStepDurationCalculatorConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return StepDurationCollectorType()
	}

	override fun createPlugin(): AverageStepDurationCalculator
	{
		return AverageStepDurationCalculator(this)
	}

	class StepDurationCollectorType : ResourceSubtype(TYPE_ID, "Average Step Duration Calculator", AverageStepDurationCalculatorConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): AverageStepDurationCalculatorConfiguration
		{
			val config = AverageStepDurationCalculatorConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(STEP_TAG_PARAM, ValueSourceConfiguration.forValue("measure"))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Measures the durations of executed steps and calculates the average for the test suite")
		}
	}

	companion object
	{

		val TYPE_ID = AverageStepDurationCalculatorConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val STEP_TAG_PARAM = "steptag"
	}
}
