package org.redis.cache;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.redis.cache.health.RedisHealthCheck;
import org.redis.cache.resources.CacheResource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class CacheService extends Application<CacheServiceConfiguration> {

	private static final String CLASS_NAME = CacheService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	public static void main(String[] args) throws Exception {
		new CacheService().run(args);
	}

	@Override
	public String getName() {
		return "cache-management";
	}

	@Override
	public void initialize(Bootstrap<CacheServiceConfiguration> bootstrap) {

		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap
				.setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

		bootstrap.addBundle(new SwaggerBundle<CacheServiceConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
					CacheServiceConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(CacheServiceConfiguration configuration,
			Environment environment) {

		// create redis client
		@SuppressWarnings("resource")
		JedisPool pool = new JedisPool(new JedisPoolConfig(), configuration
				.getRedisConfig().getEndpoint());
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			final CacheResource cachResource = new CacheResource(jedis,
					configuration.getCacheValidityTime());
			final RedisHealthCheck healthCheck = new RedisHealthCheck(jedis);
			environment.healthChecks().register("redis", healthCheck);
			environment.jersey().setUrlPattern("/api/*");
			environment.jersey().register(cachResource);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			if (jedis != null && pool != null) {
				jedis.close();
				pool.destroy();
			}
		}

	}

}
