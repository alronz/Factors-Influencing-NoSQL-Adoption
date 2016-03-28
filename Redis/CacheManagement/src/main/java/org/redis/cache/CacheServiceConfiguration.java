package org.redis.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class CacheServiceConfiguration extends Configuration {

	@JsonProperty
	private RedisConfig redis;

	@JsonProperty
	private Integer cacheValidityTime;

	public Integer getCacheValidityTime() {
		return this.cacheValidityTime;
	}

	public RedisConfig getRedisConfig() {
		return this.redis;
	}

	@JsonProperty("swagger")
	public SwaggerBundleConfiguration swaggerBundleConfiguration;

}
