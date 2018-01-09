package com.webperformance.muse.measurements.stepduration

import com.fasterxml.jackson.databind.ObjectMapper
import org.musetest.core.datacollection.TestResultData
import java.io.InputStream
import java.io.OutputStream

class StepDurationGoals : TestResultData
{
	val passes: MutableMap<Long, Long> = mutableMapOf()
	val fails: MutableMap<Long, Long> = mutableMapOf()
	
	private var name: String = "StepDurationGoals"
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
	
	fun getPasses(step_id: Long): Long
	{
		val passed = passes.get(step_id)
		if (passed == null)
			return 0
		return passed
	}
	
	fun getFails(step_id: Long): Long
	{
		val failed = fails.get(step_id)
		if (failed == null)
			return 0
		return failed
	}
	
	override fun read(instream: InputStream): Any
	{
		val mapper = ObjectMapper()
		return mapper.reader().readValue(instream)
	}
	
	fun record(stepid: Long, pass: Boolean)
	{
		val target_map: Map<Long, Long>
		if (pass)
			target_map = passes
		else
			target_map = fails
		
		val current_val = target_map.get(stepid)
		if (current_val == null)
			target_map.put(stepid, 1L)
		else
			target_map.put(stepid, current_val + 1)
	}
}