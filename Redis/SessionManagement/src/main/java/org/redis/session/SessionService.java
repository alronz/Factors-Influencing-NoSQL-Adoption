package org.redis.session;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.redis.session.health.RedisHealthCheck;
import org.redis.session.resources.SessionResource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class SessionService extends Application<SessionServiceConfiguration> {

	private static final String CLASS_NAME = SessionService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	public static void main(String[] args) throws Exception {
		new SessionService().run(args);
	}

	@Override
	public String getName() {
		return "session-management";
	}

	@Override
	public void initialize(Bootstrap<SessionServiceConfiguration> bootstrap) {

		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap
				.setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

		bootstrap.addBundle(new SwaggerBundle<SessionServiceConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
					SessionServiceConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(SessionServiceConfiguration configuration,
			Environment environment) {

		// create redis client
		@SuppressWarnings("resource")
		JedisPool pool = new JedisPool(new JedisPoolConfig(), configuration
				.getRedisConfig().getEndpoint());
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			final SessionResource sessionResource = new SessionResource(jedis,
					configuration.getSessionValidityTime());
			final RedisHealthCheck healthCheck = new RedisHealthCheck(jedis);
			environment.healthChecks().register("redis", healthCheck);
			environment.jersey().setUrlPattern("/api/*");
			environment.jersey().register(sessionResource);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			if (jedis != null && pool != null) {
				jedis.close();
				pool.destroy();
			}
		}

	}

}
