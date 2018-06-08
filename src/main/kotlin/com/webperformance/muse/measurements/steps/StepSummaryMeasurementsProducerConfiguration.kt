package com.webperformance.muse.measurements.steps

import com.fasterxml.jackson.annotation.*
import com.webperformance.muse.measurements.steps.StepSummaryMeasurementsProducerConfiguration.Companion.ADD_TEST_ID_PARAM
import com.webperformance.muse.measurements.steps.StepSummaryMeasurementsProducerConfiguration.Companion.COLLECT_SUCCESS_STEPS_PARAM
import com.webperformance.muse.measurements.steps.StepSummaryMeasurementsProducerConfiguration.Companion.COLLECT_ERRORED_STEPS_PARAM
import com.webperformance.muse.measurements.steps.StepSummaryMeasurementsProducerConfiguration.Companion.COLLECT_FAILED_STEPS_PARAM
import com.webperformance.muse.measurements.steps.StepSummaryMeasurementsProducerConfiguration.Companion.COLLECT_RUNNING_PARAM
import com.webperformance.muse.measurements.steps.StepSummaryMeasurementsProducerConfiguration.Companion.STEP_TAG_PARAM
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
@MuseTypeId("step-summary-measurements-producer")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = GenericConfigurablePlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Step tag", description = "If this parameter is present, only collect measurements on steps tagged with the value of this parameter", type = SubsourceDescriptor.Type.Named, name = STEP_TAG_PARAM, optional = true),
	MuseSubsourceDescriptor(displayName = "Count successes", description = "If this parameter is present and true, count the number of steps completed successfully", type = SubsourceDescriptor.Type.Named, name = COLLECT_SUCCESS_STEPS_PARAM, optional = true, defaultValue = "true"),
	MuseSubsourceDescriptor(displayName = "Count failures", description = "If this parameter is present and true, count the number of steps that finished with a failure status", type = SubsourceDescriptor.Type.Named, name = COLLECT_FAILED_STEPS_PARAM, optional = true, defaultValue = "true"),
	MuseSubsourceDescriptor(displayName = "Count errors", description = "If this parameter is present and true, count the number of steps that finished with an error status", type = SubsourceDescriptor.Type.Named, name = COLLECT_ERRORED_STEPS_PARAM, optional = true, defaultValue = "true"),
	MuseSubsourceDescriptor(displayName = "Running steps", description = "If this parameter is present and true, measure the number and duration of currently running steps", type = SubsourceDescriptor.Type.Named, name = COLLECT_RUNNING_PARAM, optional = true, defaultValue = "true"),
	MuseSubsourceDescriptor(displayName = "Add Test Id", description = "If this parameter is present and true, add the test id to the measurements", type = SubsourceDescriptor.Type.Named, name = ADD_TEST_ID_PARAM, optional = true, defaultValue = "true")
)
class StepSummaryMeasurementsProducerConfiguration : GenericResourceConfiguration(), PluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return StepSummaryMeasurementsProducerType()
	}

	override fun createPlugin(): StepSummaryMeasurementsProducer
	{
		return StepSummaryMeasurementsProducer(this)
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
		return isParameterTrue(context, ADD_TEST_ID_PARAM)
	}
	
	@JsonIgnore
	fun isCollectRunningSteps(context: MuseExecutionContext): Boolean
	{
		return isParameterTrue(context, COLLECT_RUNNING_PARAM)
	}
	
	@JsonIgnore
	fun isCollectSuccesses(context: MuseExecutionContext): Boolean
	{
		return isParameterTrue(context, COLLECT_SUCCESS_STEPS_PARAM)
	}
	
	@JsonIgnore
	fun isCollectFailures(context: MuseExecutionContext): Boolean
	{
		return isParameterTrue(context, COLLECT_FAILED_STEPS_PARAM)
	}
	
	@JsonIgnore
	fun isCollectErrors(context: MuseExecutionContext): Boolean
	{
		return isParameterTrue(context, COLLECT_ERRORED_STEPS_PARAM)
	}
	
	class StepSummaryMeasurementsProducerType : ResourceSubtype(TYPE_ID, "Step Summary Measurements Producer", StepSummaryMeasurementsProducerConfiguration::class.java, PluginConfiguration.PluginConfigurationResourceType())
	{

		override fun create(): StepSummaryMeasurementsProducerConfiguration
		{
			val config = StepSummaryMeasurementsProducerConfiguration()
			config.parameters().addSource(GenericConfigurablePlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(GenericConfigurablePlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(STEP_TAG_PARAM, ValueSourceConfiguration.forValue("measure"))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Creates measurements of step duration")
		}
	}

	companion object
	{
		val TYPE_ID = StepSummaryMeasurementsProducerConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val STEP_TAG_PARAM = "steptag"
		const val COLLECT_RUNNING_PARAM = "collect-running"
		const val COLLECT_SUCCESS_STEPS_PARAM = "collect-success"
		const val COLLECT_FAILED_STEPS_PARAM = "collect-failed"
		const val COLLECT_ERRORED_STEPS_PARAM = "collect-errored"
		const val ADD_TEST_ID_PARAM = "addtestid"
	}
	
}
