package com.webperformance.muse.measurements.stepduration

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.FileOutputStream

fun main(args: Array<String>)
{
	val calculator = AverageStepDurationsCalculator()
	val prefix = args[0] + "."
	val outfile_name = args[1]
	val mapper = ObjectMapper()
	
	val root = File(".")
	val files = root.listFiles()
	var processed = 0
	for (file in files)
		if (file.name.startsWith(prefix) && file.isDirectory)
		{
			val durations_file = File(file, "StepDurations.json")
			if (durations_file.exists())
			{
				val durations = mapper.readValue(durations_file, StepDurations::class.java)
				calculator.add(durations)
				processed++
			}
		}
	
	val outstream = FileOutputStream(File(outfile_name))
	calculator.getAverages().write(outstream)
	outstream.close()
	
	println("$processed files processed")
	println("averages calculated for ${calculator.getAverages().getStepIds().size} steps")
}

class AverageStepDurationsCalculator
{
	private val counts = HashMap<Long, Long>()
	private val totals = HashMap<Long, Long>()
	
	fun add(durations: StepDurations)
	{
		for (key in durations.getStepIds())
			addDurations(key, durations.getDurations(key))
	}
	
	private fun addDurations(stepid: Long, durations: List<Long>)
	{
		var count = counts.get(stepid) ?: 0
		count += durations.size
		
		var total = totals.get(stepid) ?: 0
		for (duration in durations)
			total += duration
		
		counts.put(stepid, count)
		totals.put(stepid, total)
	}
	
	fun getAverages(): StepDurations
	{
		val averages = StepDurations()
		for (key in counts.keys)
		{
			val total = totals.get(key)
			val count = counts.get(key)
			if (total != null && count != null)
				averages.record(key, total / count)
		}
		return averages
	}
}