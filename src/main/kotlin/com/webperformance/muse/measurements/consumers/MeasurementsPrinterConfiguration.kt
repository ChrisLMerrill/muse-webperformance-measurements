package com.webperformance.muse.measurements.consumers

import org.musetest.core.MuseExecutionContext
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
@MuseTypeId("measurements-printer")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Ignore subjects", description = "ignore the subject name listed here (multiple not yet supported)", type = SubsourceDescriptor.Type.Named, name = MeasurementsPrinterConfiguration.IGNORE_SUBJECT_PARAM)
)
class MeasurementsPrinterConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return MeasurementsPrinterType()
	}

	override fun createPlugin(): MeasurementsPrinter
	{
		return MeasurementsPrinter(this)
	}

    fun getIgnoredSubjects(context: MuseExecutionContext): List<String>
    {
        val ignored = mutableListOf<String>()
        val config = parameters[MeasurementsPrinterConfiguration.IGNORE_SUBJECT_PARAM]
        if (config != null)
        {
            val source = config.createSource()
            val ignore_list = source.resolveValue(context)
            if (ignore_list != null)
                ignored.add(ignore_list.toString())
        }
        return ignored
    }

	class MeasurementsPrinterType : ResourceSubtype(TYPE_ID, "Measurements Printer", MeasurementsPrinterConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): MeasurementsPrinterConfiguration
		{
			val config = MeasurementsPrinterConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(IGNORE_SUBJECT_PARAM, ValueSourceConfiguration.forValue("samples"))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Prints measurements to the console (standard out).")
		}
	}

	companion object
	{

		val TYPE_ID = MeasurementsPrinterConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
        const val IGNORE_SUBJECT_PARAM = "ignore-subject"
	}
}
