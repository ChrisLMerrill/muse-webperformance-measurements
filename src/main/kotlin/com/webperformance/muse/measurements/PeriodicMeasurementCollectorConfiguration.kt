package com.webperformance.muse.measurements

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
@MuseTypeId("periodic-measurement-collector")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Sample period", description = "Duration of measurement period (in milliseconds). Default is 10 seconds.", type = SubsourceDescriptor.Type.Named, name = PeriodicMeasurementCollectorConfiguration.PERIOD_PARAM_NAME, optional =  true)
)
class PeriodicMeasurementCollectorConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return PeriodicMeasurementCollectorType()
	}

	override fun createPlugin(): PeriodicMeasurementCollector
	{
		return PeriodicMeasurementCollector(this)
	}

	class PeriodicMeasurementCollectorType : ResourceSubtype(TYPE_ID, "Periodic Measurement Collector", PeriodicMeasurementCollectorConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): PeriodicMeasurementCollectorConfiguration
		{
			val config = PeriodicMeasurementCollectorConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Collects measurements periodically.")
		}
	}

	companion object
	{
		const val PERIOD_PARAM_NAME = "period"
		val TYPE_ID = PeriodicMeasurementCollectorConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
	}
}
