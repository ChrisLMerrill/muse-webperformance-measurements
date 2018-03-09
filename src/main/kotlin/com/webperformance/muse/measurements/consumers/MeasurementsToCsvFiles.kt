package com.webperformance.muse.measurements.consumers

import au.com.bytecode.opencsv.*
import com.fasterxml.jackson.databind.*
import com.webperformance.muse.measurements.*
import org.musetest.core.*
import org.musetest.core.events.*
import org.musetest.core.plugins.*
import org.musetest.core.resource.generic.*
import org.musetest.core.resource.json.*
import org.musetest.core.resultstorage.*
import org.musetest.core.suite.*
import org.musetest.core.util.*
import java.io.*

class MeasurementsToCsvFiles(configuration: GenericResourceConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsConsumer
{
	override fun acceptMeasurements(measurements: Measurements)
	{
		if (_mapper == null)
			return
		
		for (measurement in measurements.iterator())
		{
			val subject = measurement.metadata["subject"]
			if (subject != null)
			{
				val metric = measurement.metadata["metric"]
				val value = measurement.value
				val writer = getWriter(_folder, subject.toString())
				writer.addValue(metric.toString(), value)
			}
		}
		for (writer in _writers.values)
			writer.writeValues(_index)
		_index++
	}
	
	private fun getWriter(folder : File, subject : String) : SubjectWriter
	{
		var writer = _writers[subject]
		if (writer == null)
		{
			writer = SubjectWriter(folder, subject)
			_writers.put(subject, writer)
		}
		return writer
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		val storage_location_provider = Plugins.findType(LocalStorageLocationProvider::class.java, context)
		if (storage_location_provider == null)
			context.raiseEvent(MessageEventType.create("Could not find a LocalStorageLocation...don't know where to store the measurements. Will ignore them."))
		else
		{
			_folder = storage_location_provider.baseFolder
			_mapper = JsonMapperFactory.createMapper(TypeLocator(context.project.classLocator))
		}
		
		context.addEventListener { e ->
			run {
				if (e.typeId == EndSuiteEventType.TYPE_ID)
				{
					closeFiles()
				}
			}
		}
	}
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	fun closeFiles()
	{
		// TODO listen for test suite end event?
		
		for (writer in _writers.values)
			writer.close()
	}

	var _index = 0
	lateinit var _folder : File
	var _mapper : ObjectMapper? = null
	val _writers = HashMap<String, SubjectWriter>()
	
	class SubjectWriter(folder : File, id : String)
	{
		private val writer : CSVWriter
		private val file_writer: FileWriter
		private var header_written = false

		init
		{
			file_writer = FileWriter(File(folder, id + ".csv"))
			writer = CSVWriter(file_writer)
		}
		
		fun addValue(name : String, value : Any?)
		{
			if (!metrics.contains(name))
				metrics.add(name)
			if (value != null)
				values.put(name, value)
		}
		
		fun writeValues(sequence : Number)
		{
			writeHeader()
			
			val list = mutableListOf<String>()
			list.add(sequence.toString())
			for (metric in metrics)
			{
				if (values[metric] == null)
					list.add("")
				else
					list.add(values[metric].toString())
			}
			
			writer.writeNext(list.toTypedArray())
			values.clear()
		}
		
		private fun writeHeader()
		{
			if (header_written)
				return;
			
			val list = mutableListOf<String>()
			list.add("sequence")
			for (metric in metrics)
				list.add(metric)
			
			writer.writeNext(list.toTypedArray())
			header_written = true
		}
		
		fun close()
		{
			writer.flush()
			file_writer.close()
		}
		
		val metrics = ArrayList<String>()
		val values = HashMap<String, Any>()
	}
}