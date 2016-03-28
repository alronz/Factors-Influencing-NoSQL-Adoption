package org.redis.analytics;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;





import org.redis.analytics.health.RedisHealthCheck;
import org.redis.analytics.resources.AnalyticsResource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class AnalyticsService extends Application<AnalyticsServiceConfiguration> {

	private static final String CLASS_NAME = AnalyticsService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	public static void main(String[] args) throws Exception {
		new AnalyticsService().run(args);
	}

	@Override
	public String getName() {
		return "analytics";
	}

	@Override
	public void initialize(Bootstrap<AnalyticsServiceConfiguration> bootstrap) {

		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap
				.setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

		bootstrap.addBundle(new SwaggerBundle<AnalyticsServiceConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
					AnalyticsServiceConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(AnalyticsServiceConfiguration configuration,
			Environment environment) {

		// create redis client
		@SuppressWarnings("resource")
		JedisPool pool = new JedisPool(new JedisPoolConfig(), configuration
				.getRedisConfig().getEndpoint());
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			final AnalyticsResource analyticsResource = new AnalyticsResource(jedis);
			final RedisHealthCheck healthCheck = new RedisHealthCheck(jedis);
			environment.healthChecks().register("redis", healthCheck);
			environment.jersey().setUrlPattern("/api/*");
			environment.jersey().register(analyticsResource);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			if (jedis != null && pool != null) {
				jedis.close();
				pool.destroy();
			}
		}

	}

}
