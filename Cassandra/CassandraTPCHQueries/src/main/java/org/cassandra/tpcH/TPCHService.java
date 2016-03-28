package org.cassandra.tpcH;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
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

import org.cassandra.tpcH.health.CassandraHealthCheck;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.cassandra.tpcH.resources.TPCHResource;

import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class TPCHService extends Application<TPCHServiceConfiguration> {

    private static final String CLASS_NAME = TPCHService.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static void main(String[] args) throws Exception {
        new TPCHService().run(args);
    }

    @Override
    public String getName() {
        return "cassandra-test";
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

    @Override
    public void run(TPCHServiceConfiguration configuration,
                    Environment environment) {

        Cluster cluster = null;
        Session session = null;

        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();

        session = cluster.connect();

        ResultSet rs = session.execute("select release_version from system.local");
        Row row = rs.one();
        LOGGER.info(row.getString("release_version"));
        session.close();


        if (session != null) {


            TPCHModel model = new TPCHModel(cluster);
            model.connect();
            model.initialiseData();
            model.close();

            TPCHResource tpcHResource = new TPCHResource(model);
            CassandraHealthCheck healthCheck = new CassandraHealthCheck(session, "SELECT * FROM system.schema_keyspaces");
            environment.healthChecks().register("cassandra", healthCheck);
            environment.jersey().setUrlPattern("/api/*");
            environment.jersey().register(tpcHResource);
        }
    }
}
