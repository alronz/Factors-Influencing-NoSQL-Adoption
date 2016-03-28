package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class TestServiceConfiguration extends Configuration {

	@JsonProperty
	private RedisConfig redis;

	public RedisConfig getRedisConfig() {
		return this.redis;
	}

	@JsonProperty("swagger")
	public SwaggerBundleConfiguration swaggerBundleConfiguration;

}
