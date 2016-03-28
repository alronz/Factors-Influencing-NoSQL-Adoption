package org.neo4j.tpcH.health;

import org.neo4j.graphdb.GraphDatabaseService;

import com.codahale.metrics.health.HealthCheck;

public class Neo4jHealthCheck extends HealthCheck {

	GraphDatabaseService graphDb;

	public Neo4jHealthCheck(GraphDatabaseService graphDb) {

		this.graphDb = graphDb;
	}

	@Override
	protected Result check() throws Exception {

		if (!this.graphDb.isAvailable(10000)) {
			return Result.unhealthy("Neo4j isn't available! ");
		}

		return Result.healthy();
	}

}