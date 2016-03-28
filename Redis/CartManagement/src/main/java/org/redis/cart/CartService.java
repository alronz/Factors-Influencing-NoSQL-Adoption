package org.redis.cart;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.redis.cart.health.RedisHealthCheck;
import org.redis.cart.resources.CartResource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class CartService extends Application<CartServiceConfiguration> {

	private static final String CLASS_NAME = CartService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	public static void main(String[] args) throws Exception {
		new CartService().run(args);
	}

	@Override
	public String getName() {
		return "cart-management";
	}

	@Override
	public void initialize(Bootstrap<CartServiceConfiguration> bootstrap) {

		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap
				.setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

		bootstrap.addBundle(new SwaggerBundle<CartServiceConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
					CartServiceConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(CartServiceConfiguration configuration,
			Environment environment) {

		// create redis client
		@SuppressWarnings("resource")
		JedisPool pool = new JedisPool(new JedisPoolConfig(), configuration
				.getRedisConfig().getEndpoint());
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			final CartResource cartResource = new CartResource(jedis,
					configuration.getCartValidityTime());
			final RedisHealthCheck healthCheck = new RedisHealthCheck(jedis);
			environment.healthChecks().register("redis", healthCheck);
			environment.jersey().setUrlPattern("/api/*");
			environment.jersey().register(cartResource);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			if (jedis != null && pool != null) {
				jedis.close();
				pool.destroy();
			}
		}

	}

}
