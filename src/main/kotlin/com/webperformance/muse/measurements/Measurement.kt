package com.webperformance.muse.measurements

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Holds a measured value and metadata describing the measurements.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Measurement(val value: Number?, @JsonProperty val metadata: MutableMap<String, Any> = mutableMapOf())
{
	fun addMetadata(name: String, value: Any)
	{
		metadata[name] = value
	}
	
	fun clone() : Measurement
	{
		return Measurement(value, metadata.toMutableMap())
	}
	
	companion object
	{
		const val META_METRIC = "metric"
		const val META_SUBJECT = "subject"
		const val META_SUBJECT_TYPE = "subj_type"
		const val META_SEQUENCE = "sequence"
		const val META_TIMESTAMP = "time" // milliseconds
		const val META_TEST = "test"
		const val META_STATUS = "status"
		const val META_SOURCE_HOST = "source_host"
	}
}
