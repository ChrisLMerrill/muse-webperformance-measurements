package com.webperformance.muse.measurements

import org.musetest.core.*
import org.musetest.core.events.*

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
class GoalAssessmentEventType : EventType(TYPE_ID, "Goal Assessment")
{
	companion object
	{

		fun create(goal_passed: Boolean, message: String): MuseEvent
		{
			val event = MuseEvent(TYPE_ID)
			if (!goal_passed)
				event.addTag(MuseEvent.FAILURE)
			event.setAttribute(MuseEvent.DESCRIPTION, message)
			return event
		}

		val TYPE_ID = "goal-assessment"
	}
}


