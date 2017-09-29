package com.webperformance.muse.measurements


import org.musetest.core.MuseTypeId
import org.musetest.core.context.ContextInitializer
import org.musetest.core.context.initializers.ContextInitializerConfiguration
import org.musetest.core.resource.types.ResourceSubtype
import org.musetest.core.resource.types.ResourceType

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("data-collector-init")
class DataCollectionInitializerConfiguration : ContextInitializerConfiguration()
{
	override fun createInitializer(): ContextInitializer
	{
		return StepDurationCollector()
	}

	override fun getType(): ResourceType
	{
		return DataCollectionInitializerResourceType()
	}

	// discovered and instantiated by reflection (see class ResourceTypes)
	class DataCollectionInitializerResourceType : ResourceSubtype(DataCollectionInitializerConfiguration::class.java.getAnnotation(MuseTypeId::class.java).value, "Data Collection Initializer", DataCollectionInitializerConfiguration::class.java, ContextInitializerConfiguration.ContextInitializerResourceType())
}


