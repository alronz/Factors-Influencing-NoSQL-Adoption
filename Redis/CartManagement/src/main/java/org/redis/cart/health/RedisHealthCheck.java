package org.redis.cart.health;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.codahale.metrics.health.HealthCheck;

import redis.clients.jedis.Jedis;

public class RedisHealthCheck extends HealthCheck {
	private final Jedis jedis;

	private static final String CLASS_NAME = RedisHealthCheck.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	public RedisHealthCheck(Jedis jedis) {
		this.jedis = jedis;
	}

	@Override
	protected Result check() throws Exception {
		try {
			final String pong = this.jedis.ping();
			if ("PONG".equals(pong)) {
				return Result.healthy();
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		return Result.unhealthy("Could not ping redis");
	}
}