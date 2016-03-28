package org.redis.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RedisConfig {

	@JsonProperty
	private String endpoint = "";

	public String getEndpoint() {
		return this.endpoint;
	}

}
