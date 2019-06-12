package com.webperformance.muse.measurements.steps

import com.fasterxml.jackson.annotation.*
import org.musetest.core.*
import org.musetest.core.plugins.*
import org.musetest.core.resource.generic.*
import org.musetest.core.resource.types.*
import org.musetest.core.values.*
import org.musetest.core.values.descriptor.*

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("step-measurements-producer")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Step tag", description = "If this parameter is present, only collect measurements on steps tagged with the value of this parameter", type = SubsourceDescriptor.Type.Named, name = StepMeasurementsProducerConfiguration.STEP_TAG_PARAM, optional = true),
	MuseSubsourceDescriptor(displayName = "Overall Average", description = "If true, compute the overall average duration of all measured steps", type = SubsourceDescriptor.Type.Named, name = StepMeasurementsProducerConfiguration.OVERALL_AVG_PARAM, optional = true, defaultValue = "true"),
	MuseSubsourceDescriptor(displayName = "Completed Steps", description = "If true, count total steps completed", type = SubsourceDescriptor.Type.Named, name = StepMeasurementsProducerConfiguration.OVERALL_COMPLETED_PARAM, optional = true, defaultValue = "true")
)
class StepMeasurementsProducerConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return StepMeasurementsProducerType()
	}

	override fun createPlugin(): StepMeasurementsProducer
	{
		return StepMeasurementsProducer(this)
	}

	fun countTotalSteps(context: MuseExecutionContext): Boolean
	{
		return isParamTrue(context, OVERALL_COMPLETED_PARAM)
	}
	
	fun calculateOverallAverageDuration(context: MuseExecutionContext): Boolean
	{
		return isParamTrue(context, OVERALL_AVG_PARAM)
	}

	fun isParamTrue(context: MuseExecutionContext, param: String): Boolean
	{
		if (parameters != null && parameters.containsKey(param))
		{
			val config = parameters[param]
			if (config != null)
			{
				val source = config.createSource(context.project)
				val value = source.resolveValue(context)
				if (value is Boolean)
					return value
				if ("true".equals(value.toString().toLowerCase()))
					return true
			}
		}
		return false
	}
	
	@JsonIgnore
	fun getStepTag(context: MuseExecutionContext): String?
	{
		if (parameters != null && parameters.containsKey(STEP_TAG_PARAM))
		{
			val step_tag_source_config = parameters[STEP_TAG_PARAM]
			if (step_tag_source_config != null)
			{
				val tag_source = step_tag_source_config.createSource(context.project)
				return tag_source.resolveValue(context).toString()
			}
		}
		return null
	}
	
	class StepMeasurementsProducerType : ResourceSubtype(TYPE_ID, "Step Measurements Producer", StepMeasurementsProducerConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): StepMeasurementsProducerConfiguration
		{
			val config = StepMeasurementsProducerConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(STEP_TAG_PARAM, ValueSourceConfiguration.forValue("measure"))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Collects aggregated measurements of step execution")
		}
	}

	companion object
	{
		val TYPE_ID = StepMeasurementsProducerConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val STEP_TAG_PARAM = "steptag"
		const val OVERALL_AVG_PARAM = "overall-avg"
		const val OVERALL_COMPLETED_PARAM = "ovarall-completed"
	}
	
}
