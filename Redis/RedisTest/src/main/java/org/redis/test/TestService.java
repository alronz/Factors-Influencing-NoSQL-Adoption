package org.redis.test;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.redis.test.health.RedisHealthCheck;
import org.redis.test.resources.InspectDataResource;
import org.redis.test.resources.InventoryResource;
import org.redis.test.resources.JobQueueResource;
import org.redis.test.resources.OrderResource;
import org.redis.test.resources.TestResource;
import org.redis.test.resources.VIPResource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class TestService extends Application<TestServiceConfiguration> {

	private static final String CLASS_NAME = TestService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	public static void main(String[] args) throws Exception {
		new TestService().run(args);
	}

	@Override
	public String getName() {
		return "test-redis";
	}

	@Override
	public void initialize(Bootstrap<TestServiceConfiguration> bootstrap) {

		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap
				.setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

		bootstrap.addBundle(new SwaggerBundle<TestServiceConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
					TestServiceConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(TestServiceConfiguration configuration,
			Environment environment) {

		// create redis client
		@SuppressWarnings("resource")
		JedisPool pool = new JedisPool(new JedisPoolConfig(), configuration
				.getRedisConfig().getEndpoint());
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			final TestResource testResource = new TestResource(jedis);
			final JobQueueResource jobResource = new JobQueueResource(jedis);
			final RedisHealthCheck healthCheck = new RedisHealthCheck(jedis);
			final InspectDataResource inspectDataResource = new InspectDataResource(
					jedis);
			final VIPResource vipResource = new VIPResource(jedis);
			final InventoryResource inventoryResource = new InventoryResource(
					jedis);
			final OrderResource orderResource = new OrderResource(jedis);
			environment.healthChecks().register("redis", healthCheck);
			environment.jersey().setUrlPattern("/api/*");
			environment.jersey().register(testResource);
			environment.jersey().register(jobResource);
			environment.jersey().register(inspectDataResource);
			environment.jersey().register(vipResource);
			environment.jersey().register(inventoryResource);
			environment.jersey().register(orderResource);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			if (jedis != null && pool != null) {
				jedis.close();
				pool.destroy();
			}
		}

	}

}
