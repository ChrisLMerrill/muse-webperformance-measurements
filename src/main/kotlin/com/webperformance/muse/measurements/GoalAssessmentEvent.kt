package com.webperformance.muse.measurements

import org.musetest.core.MuseEvent
import org.musetest.core.events.EventType

class GoalAssessmentEvent(var goalSatisfied: Boolean): MuseEvent(TYPE)
{
	override fun getDescription(): String
	{
		if (goalSatisfied)
			return "Goal succeeded"
		else
			return "Goal failed"
	}
	
	class GoalAssessmentEventType : EventType()
	{
		override fun getTypeId(): String
		{
			return TYPE_ID
		}

		override fun getName(): String?
		{
			return null
		}

		companion object
		{
			val TYPE_ID = "goal-assessment"
		}
	}
	
	companion object
	{

		val TYPE = GoalAssessmentEventType()
	}
	
}