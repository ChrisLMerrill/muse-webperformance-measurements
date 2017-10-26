package com.webperformance.muse.measurements

import com.fasterxml.jackson.databind.ObjectMapper
import org.musetest.core.datacollection.TestResultData
import java.io.OutputStream

class StepDurations : TestResultData
{
	val durations: MutableList<Measurement> = mutableListOf()
	
	private var name: String? = null
	override fun getName(): String?
	{
		return name
	}

	override fun setName(name: String)
	{
		this.name = name
	}

	override fun suggestFilename(): String
	{
		return name!! + ".json"
	}

	override fun write(outstream: OutputStream)
	{
		val mapper = ObjectMapper()
		mapper.writerWithDefaultPrettyPrinter().writeValue(outstream, this)
	}
}