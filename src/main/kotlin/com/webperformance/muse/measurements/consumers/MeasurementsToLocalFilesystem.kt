package com.webperformance.muse.measurements.consumers

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

class MeasurementsToLocalFilesystem(configuration: GenericResourceConfiguration) : GenericConfigurablePlugin(configuration), MeasurementsConsumer
{
	override fun acceptMeasurements(measurements: Measurements)
	{
		if (_folder == null || _mapper == null)
			return;
		
/* print to console
		for (measurement in measurements.iterator())
		{
			val builder = StringBuilder()
			for (name in measurement.metadata.keys)
			{
				if (builder.length > 0)
					builder.append(", ")
				builder.append(name)
				builder.append("=")
				builder.append(measurement.metadata[name])
			}
			getStream().println("${measurement.value} : ${builder}")
		}
*/

		val file = File(_folder, "ms" + _index++ + ".json")
		_mapper?.writeValue(file, measurements)
	}
	
	override fun initialize(context: MuseExecutionContext)
	{
		val storage_location_provider = Plugins.findType(LocalStorageLocationPlugin::class.java, context)
		if (storage_location_provider == null)
			context.raiseEvent(MessageEventType.create("Could not find a LocalStorageLocation...don't know where to store the measurements. Will ignore them."))
		else
		{
			_folder = storage_location_provider.baseFolder
			_mapper = JsonMapperFactory.createMapper(TypeLocator(context.project.classLocator))
		}
	}
	
	override fun applyToContextType(context: MuseExecutionContext?): Boolean
	{
		return context is TestSuiteExecutionContext
	}
	
	private fun getStream() : PrintStream
		{
		return _stream
		}
	
	fun setStream(stream: PrintStream)
	{
		_stream = stream;
	}
	
	var _index = 0
	var _stream = System.out
	var _folder : File? = null
	var _mapper : ObjectMapper? = null
}