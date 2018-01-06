package com.webperformance.muse.measurements.stepduration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import org.musetest.core.datacollection.TestResultData
import java.io.InputStream
import java.io.OutputStream

class StepDurations : TestResultData
{
	val durations: MutableMap<Long, MutableList<Long>> = mutableMapOf()
	
	private var name: String = "StepDurations"
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
	
	@JsonIgnore
	fun getStepIds(): Set<Long>
	{
		return durations.keys
	}
	
	@JsonIgnore
	fun getDurations(step_id: Long): List<Long>
	{
		val list = durations.get(step_id)
		if (list == null)
			return emptyList()
		else
			return list
	}
	
	override fun read(instream: InputStream): Any
	{
		val mapper = ObjectMapper()
		return mapper.reader().readValue(instream)
	}
	
	fun record(stepid: Long, duration: Long)
	{
		var list = durations.get(stepid)
		if (list == null)
		{
			list = mutableListOf()
			durations.put(stepid, list)
		}
		list.add(duration)
		
	}
}