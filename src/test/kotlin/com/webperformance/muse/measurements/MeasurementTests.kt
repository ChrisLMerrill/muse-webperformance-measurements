package com.webperformance.muse.measurements

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.webperformance.muse.measurements.containers.MeasurementsWithCommonMetadata
import org.junit.Assert
import org.junit.Test
import java.util.*

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
	
	@Test
	fun serializeTwoWithCommonMetadata()
	{
		val m1 = Measurement(1)
		m1.addMetadata("md1", "aaa")
		val m2 = Measurement(2)
		m2.addMetadata("md2", "bbb")
		
		val group = MeasurementsWithCommonMetadata()
		group.addMeasurement(m1)
		group.addMeasurement(m2)
		group.metadata["common"] = "ccc"
		
		val copy = serialize(group)
		
		Assert.assertTrue(Objects.equals(group, copy))
		Assert.assertEquals("ccc", copy.metadata["common"])
		Assert.assertEquals(1, copy.measurements[0].value)
		Assert.assertEquals(2, copy.measurements[1].value)
		Assert.assertEquals("aaa", copy.measurements[0].metadata["md1"])
		Assert.assertEquals("bbb", copy.measurements[1].metadata["md2"])
	}
	
	private fun serialize(m1: Measurement): Measurement
	{
		val mapper = ObjectMapper().registerModule(KotlinModule())
		val file = createTempFile()
		mapper.writeValue(file, m1)
		
		val copy = mapper.readValue(file, Measurement::class.java)
		
		Assert.assertEquals(m1, copy)
		return copy
	}

	private fun serialize(m1: MeasurementsWithCommonMetadata): MeasurementsWithCommonMetadata
	{
		val mapper = ObjectMapper().registerModule(KotlinModule())
		val file = createTempFile()
		mapper.writeValue(file, m1)
		
		val copy = mapper.readValue(file, MeasurementsWithCommonMetadata::class.java)
		
		Assert.assertEquals(m1, copy)
		return copy
	}
	
}