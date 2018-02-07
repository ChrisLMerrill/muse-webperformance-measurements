package com.webperformance.muse.measurements.containers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.webperformance.muse.measurements.Measurement
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream

class MeasurementsWithCommonMetadataTests
{
	@Test fun addMetadataToMeasurement()
	{
		val container = setup()
		
		val iterator = container.iterator()
		val out1 = iterator.next()
		
		Assert.assertEquals(77, out1.value)
		Assert.assertEquals(23, out1.metadata["omd"])
		Assert.assertEquals(8, out1.metadata["nmd"])
		
		val out2 = iterator.next()
		
		Assert.assertEquals(44, out2.value)
		Assert.assertNull(out2.metadata["omd"])
		Assert.assertEquals(8, out2.metadata["nmd"])
	}
	
	private fun setup(): MeasurementsWithCommonMetadata
	{
		val m1 = Measurement(77)
		m1.addMetadata("omd", 23)
		val container = MeasurementsWithCommonMetadata()
		container.metadata.put("nmd", 8)
		container.measurements.add(m1)
		
		val m2 = Measurement(44)
		container.measurements.add(m2)
		
		return container
	}
	
	@Test fun serializeWithoutDuplicatingCommonMetadata()
	{
		val container = setup()
		val mapper = ObjectMapper().registerModule(KotlinModule())
		val outstream = ByteArrayOutputStream()
		mapper.writeValue(outstream, container)
		
		val json = outstream.toString()
		
		Assert.assertEquals(1, countSubstring(json, "77"))
		Assert.assertEquals(1, countSubstring(json, "omd"))
		Assert.assertEquals(1, countSubstring(json, "23"))
		Assert.assertEquals(1, countSubstring(json, "nmd"))
		Assert.assertEquals(1, countSubstring(json, "8"))
		Assert.assertEquals(1, countSubstring(json, "44"))
	}
	
	fun countSubstring(s: String, sub: String): Int = s.split(sub).size - 1
	
}