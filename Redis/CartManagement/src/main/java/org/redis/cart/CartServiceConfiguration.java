package org.redis.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class CartServiceConfiguration extends Configuration {

	@JsonProperty
	private RedisConfig redis;

	@JsonProperty
	private Integer cartValidityTime;

	public Integer getCartValidityTime() {
		return this.cartValidityTime;
	}

	public RedisConfig getRedisConfig() {
		return this.redis;
	}

	@JsonProperty("swagger")
	public SwaggerBundleConfiguration swaggerBundleConfiguration;

}
