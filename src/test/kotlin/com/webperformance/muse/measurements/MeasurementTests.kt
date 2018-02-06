package com.webperformance.muse.measurements

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Assert
import org.junit.Test

class MeasurementTests
{
	@Test
	fun serializeMeasurement()
	{
		val m1 = Measurement(1)
		val copy = serialize(m1)
		Assert.assertNotEquals(copy, Measurement(2))
	}
	
	@Test
	fun serializeWithMetadata()
	{
		val m1 = Measurement(7)
		m1.addMetadata("d1", "abc")
		
		val copy = serialize(m1)
		
		m1.addMetadata("d2", "def")
		Assert.assertNotEquals(m1, copy)
	}
	
	private fun serialize(m1: Measurement): Measurement?
	{
		val mapper = ObjectMapper().registerModule(KotlinModule())
		val file = createTempFile()
		mapper.writeValue(file, m1)
		
		val copy = mapper.readValue(file, Measurement::class.java)
		
		Assert.assertEquals(m1, copy)
		return copy
	}
	
}