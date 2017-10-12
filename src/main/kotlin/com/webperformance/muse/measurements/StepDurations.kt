package com.webperformance.muse.measurements

import com.fasterxml.jackson.databind.ObjectMapper
import org.musetest.core.datacollection.TestResultData
import java.io.OutputStream

class StepDurations : TestResultData
{
	val durations: MutableList<Measurement> = mutableListOf()
	private var _name: String = "durations"

	override fun setName(name: String)
	{
		_name = name
	}

	override fun getName(): String
	{
		return _name
	}

	override fun write(outstream: OutputStream)
	{
		val mapper = ObjectMapper()
		mapper.writerWithDefaultPrettyPrinter().writeValue(outstream, this)
	}

}