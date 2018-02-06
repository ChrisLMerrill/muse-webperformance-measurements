package com.webperformance.muse.measurements

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Measurement(val value: Number, @JsonProperty val metadata: MutableMap<String, Any> = mutableMapOf())
{
	fun addMetadata(name: String, value: Any)
	{
		metadata[name] = value
	}
}
