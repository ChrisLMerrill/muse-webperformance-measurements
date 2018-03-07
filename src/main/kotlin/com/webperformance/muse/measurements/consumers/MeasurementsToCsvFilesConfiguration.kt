package com.webperformance.muse.measurements.consumers

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
@MuseTypeId("measurements-to-csv-files")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM)
)
class MeasurementsToCsvFilesConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return MeasurementsToCsvFilesType()
	}

	override fun createPlugin(): MeasurementsToCsvFiles
	{
		return MeasurementsToCsvFiles(this)
	}

	class MeasurementsToCsvFilesType : ResourceSubtype(TYPE_ID, "Store Measurements to CSV files", MeasurementsToCsvFilesConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): MeasurementsToCsvFilesConfiguration
		{
			val config = MeasurementsToCsvFilesConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Stores measurements as CSV files on the local filesystem. Requires the LocalStorageLocation plugin.")
		}
	}

	companion object
	{

		val TYPE_ID = MeasurementsToCsvFilesConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
	}
}
