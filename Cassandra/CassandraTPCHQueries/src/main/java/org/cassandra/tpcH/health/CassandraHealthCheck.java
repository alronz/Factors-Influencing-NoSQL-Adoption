package org.cassandra.tpcH.health;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraHealthCheck extends HealthCheck {

	private static final Logger LOG = LoggerFactory.getLogger(CassandraHealthCheck.class);

	private final Session session;
	private final String validationQuery;

	public CassandraHealthCheck(Session session, String validationQuery) {
		this.session = session;
		this.validationQuery = validationQuery;
	}

	@Override
	protected Result check() throws Exception {
		try {
			session.execute(validationQuery);
			return Result.healthy();
		} catch (Exception ex) {
			LOG.error("Unable to connect to Cassandra cluster [{}]", session.getCluster().getClusterName(), ex);
			throw ex;
		}
	}
}