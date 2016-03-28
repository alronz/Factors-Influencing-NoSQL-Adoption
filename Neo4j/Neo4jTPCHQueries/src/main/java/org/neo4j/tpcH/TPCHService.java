package org.neo4j.tpcH;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tpcH.health.Neo4jHealthCheck;
import org.neo4j.tpcH.resources.TPCHResource;

import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class TPCHService extends Application<TPCHServiceConfiguration> {

	private static final String CLASS_NAME = TPCHService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	public static void main(String[] args) throws Exception {
		new TPCHService().run(args);
	}

	@Override
	public String getName() {
		return "neo4j-test";
	}

	@Override
	public void initialize(Bootstrap<TPCHServiceConfiguration> bootstrap) {

		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap
				.setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

		bootstrap.addBundle(new SwaggerBundle<TPCHServiceConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
					TPCHServiceConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Crl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	@Override
	public void run(TPCHServiceConfiguration configuration,
			Environment environment) {

		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(configuration.getNeo4jPath());
		registerShutdownHook(graphDb);
		

		TPCHResource tpcHResource = new TPCHResource(graphDb);

		TPCHModel model = new TPCHModel(graphDb);
		
		model.initialiseData();

		Neo4jHealthCheck healthCheck = new Neo4jHealthCheck(graphDb);
		environment.healthChecks().register("neo4j", healthCheck);
		environment.jersey().setUrlPattern("/api/*");
		environment.jersey().register(tpcHResource);

	}
}
