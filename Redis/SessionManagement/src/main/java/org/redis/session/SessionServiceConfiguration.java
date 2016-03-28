package org.redis.session;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class SessionServiceConfiguration extends Configuration {

	@JsonProperty
	private RedisConfig redis;

	@JsonProperty
	private Integer sessionValidityTime;

	public Integer getSessionValidityTime() {
		return this.sessionValidityTime;
	}

	public RedisConfig getRedisConfig() {
		return this.redis;
	}

	@JsonProperty("swagger")
	public SwaggerBundleConfiguration swaggerBundleConfiguration;

}
