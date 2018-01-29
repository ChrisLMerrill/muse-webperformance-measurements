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
@MuseTypeId("step-duration-goal-assessor")
@MuseSubsourceDescriptors(
	MuseSubsourceDescriptor(displayName = "Apply automatically?", description = "If this source resolves to true, this plugin configuration will be automatically applied to tests", type = SubsourceDescriptor.Type.Named, name = BaseTestPlugin.AUTO_APPLY_PARAM),
	MuseSubsourceDescriptor(displayName = "Apply only if", description = "Apply only if this source this source resolves to true", type = SubsourceDescriptor.Type.Named, name = BaseTestPlugin.APPLY_CONDITION_PARAM),
	MuseSubsourceDescriptor(displayName = "Step tag", description = "If this parameter is present, only assess duration goals on steps tagged with the value of this parameter", type = SubsourceDescriptor.Type.Named, name = StepDurationGoalAssessorConfiguration.STEP_TAG_PARAM),
	MuseSubsourceDescriptor(displayName = "Collect pass/fails", description = "If this parameter is present and true, collect pass/fail totals", type = SubsourceDescriptor.Type.Named, name = StepDurationGoalAssessorConfiguration.COLLECT_GOALS_PARAM, optional = true),
	MuseSubsourceDescriptor(displayName = "Goal", description = "The step duration goal (in ms)", type = SubsourceDescriptor.Type.Named, name = StepDurationGoalAssessorConfiguration.GOAL_PARAM),
	MuseSubsourceDescriptor(displayName = "Custom goal name", description = "If present, this parameter provides the name of a metadata attribute that may be added to steps to specify a custom goal for that step (in ms)", type = SubsourceDescriptor.Type.Named, name = StepDurationGoalAssessorConfiguration.GOAL_NAME_PARAM, optional = true)
)
class StepDurationGoalAssessorConfiguration : GenericResourceConfiguration(), TestPluginConfiguration
{
	override fun getType(): ResourceType?
	{
		return StepDurationGoalAssessorType()
	}

	override fun createPlugin(): StepDurationGoalAssessor
	{
		return StepDurationGoalAssessor(this)
	}

	class StepDurationGoalAssessorType : ResourceSubtype(TYPE_ID, "Step Duration Goal Assessor", StepDurationGoalAssessorConfiguration::class.java, TestPluginConfiguration.TestPluginConfigurationResourceType())
	{

		override fun create(): StepDurationGoalAssessorConfiguration
		{
			val config = StepDurationGoalAssessorConfiguration()
			config.parameters().addSource(BaseTestPlugin.AUTO_APPLY_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(BaseTestPlugin.APPLY_CONDITION_PARAM, ValueSourceConfiguration.forValue(true))
			config.parameters().addSource(STEP_TAG_PARAM, ValueSourceConfiguration.forValue("assess-goal"))
			config.parameters().addSource(GOAL_PARAM, ValueSourceConfiguration.forValue(1000L))
			return config
		}

		override fun getDescriptor(): ResourceDescriptor
		{
			return DefaultResourceDescriptor(this, "Compare the duration of the step to the goal.")
		}
	}

	companion object
	{

		val TYPE_ID = StepDurationGoalAssessorConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value
		const val STEP_TAG_PARAM = "steptag"
		const val COLLECT_GOALS_PARAM = "collect"
		const val GOAL_PARAM = "goal"
		const val GOAL_NAME_PARAM = "custom-goal-name"
	}
}
