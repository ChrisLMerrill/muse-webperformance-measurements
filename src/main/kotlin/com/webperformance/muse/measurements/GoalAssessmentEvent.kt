package com.webperformance.muse.measurements

import org.musetest.core.MuseEvent
import org.musetest.core.events.EventType

class GoalAssessmentEvent: MuseEvent
{
	constructor(type: EventType) : super(type)
}