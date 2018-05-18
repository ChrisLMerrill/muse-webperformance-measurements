package com.webperformance.muse.measurements.steps

import com.fasterxml.jackson.annotation.*
import com.webperformance.muse.measurements.steps.StepDurationProducerConfiguration.Companion.ADD_TEST_ID_PARAM
import com.webperformance.muse.measurements.steps.StepDurationProducerConfiguration.Companion.STEP_TAG_PARAM
import org.musetest.core.*
import org.musetest.core.plugins.*
import org.musetest.core.resource.generic.*
import org.musetest.core.resource.types.*
import org.musetest.core.values.*
import org.musetest.core.values.descriptor.*

/**
 * Configuration for the StepDurationProducer. This is what the user creates in the UI and is used to
 * create the producer that does the work.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("step-duration-measurements-producer")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Step tag", description = "If this parameter is present, only collect measurements on steps tagged with the value of this parameter", type = SubsourceDescriptor.Type.Named, name = STEP_TAG_PARAM, optional = true),
	MuseSubsourceDescriptor(displayName = "Add Test Id", description = "If this parameter is present and true, add the test id to the measurements", type = SubsourceDescriptor.Type.Named, name = ADD_TEST_ID_PARAM, optional = true)
)
class StepDurationProducerConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return StepDurationProducerType()
	}

	override fun createPlugin(): StepDurationProducer
	{
		return StepDurationProducer(this)
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
	
	@JsonIgnore
	fun isAddTestId(context: MuseExecutionContext): Boolean
	{
		if (parameters != null && parameters.containsKey(ADD_TEST_ID_PARAM))
		{
			val add_testid_config = parameters[ADD_TEST_ID_PARAM]
			if (add_testid_config != null)
			{
				val tag_source = add_testid_config.createSource(context.project)
				val value = tag_source.resolveValue(context)
				if (value is Boolean && value)
					return true
			}
		}
		return false
	}
	
	class StepDurationProducerType : ResourceSubtype(TYPE_ID, "Step Duration Producer", StepDurationProducerConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): StepDurationProducerConfiguration
		{
			val config = StepDurationProducerConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(STEP_TAG_PARAM, ValueSourceConfiguration.forValue("measure"))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Collects measurements of step duration")
		}
	}

	companion object
	{
		val TYPE_ID = StepDurationProducerConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val STEP_TAG_PARAM = "steptag"
		const val ADD_TEST_ID_PARAM = "addtestid"
	}
	
}
