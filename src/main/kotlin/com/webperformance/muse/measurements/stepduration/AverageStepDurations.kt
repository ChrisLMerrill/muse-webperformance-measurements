package com.webperformance.muse.measurements.stepduration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import org.musetest.core.datacollection.TestResultData
import java.io.InputStream
import java.io.OutputStream

class AverageStepDurations : TestResultData
{
	val averages: MutableMap<Long, Long> = mutableMapOf()
	
	private var name: String = "AverageStepDurations"
	override fun getName(): String
	{
		return name
	}

	override fun setName(name: String)
	{
		this.name = name
	}

	override fun suggestFilename(): String
	{
		return name + ".json"
	}

	override fun write(outstream: OutputStream)
	{
		val mapper = ObjectMapper()
		mapper.writerWithDefaultPrettyPrinter().writeValue(outstream, this)
	}
	
	override fun read(instream: InputStream): Any
	{
		val mapper = ObjectMapper()
		return mapper.reader().readValue(instream)
	}
	
	@JsonIgnore
	fun getStepIds(): Set<Long>
	{
		return averages.keys
	}
	
	@JsonIgnore
	fun getAverage(step_id: Long): Long?
	{
		return averages.get(step_id)
	}
}