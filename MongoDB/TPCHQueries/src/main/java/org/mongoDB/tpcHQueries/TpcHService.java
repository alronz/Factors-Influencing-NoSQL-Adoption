package org.mongoDB.tpcHQueries;

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
import org.mongoDB.tpcHQueries.health.MongoHealthCheck;
import org.mongoDB.tpcHQueries.resources.TpcHDenormalizedModelResource;
import org.mongoDB.tpcHQueries.resources.TpcHMixedModelResource;
import org.mongoDB.tpcHQueries.resources.TpcHNormalizedModelResource;

import com.mongodb.MongoClient;

import de.thomaskrille.dropwizard.environment_configuration.EnvironmentConfigurationFactoryFactory;

public class TpcHService extends Application<TpcHServiceConfiguration> {

    private static final String CLASS_NAME = TpcHService.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static void main(String[] args) throws Exception {
        new TpcHService().run(args);
    }

    @Override
    public String getName() {
        return "mongodb-test";
    }

    @Override
    public void initialize(Bootstrap<TpcHServiceConfiguration> bootstrap) {

        bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
        bootstrap
                .setConfigurationFactoryFactory(new EnvironmentConfigurationFactoryFactory());

        bootstrap.addBundle(new SwaggerBundle<TpcHServiceConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                    TpcHServiceConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(TpcHServiceConfiguration configuration,
                    Environment environment) {

        MongoClient mongoClient = new MongoClient();

        if (mongoClient.getDatabase("mydb") != null) {
            mongoClient.dropDatabase("mydb");
        }

        NormalizedExampleModel normalizedExampleModel = new NormalizedExampleModel(mongoClient);
        normalizedExampleModel.initialiseData();

        MixedExampleModel mixedExampleModel = new MixedExampleModel(mongoClient);
        mixedExampleModel.initialiseData();

        DenormalizedExampleModel denormalizedExampleModel = new DenormalizedExampleModel(mongoClient);
        denormalizedExampleModel.initialiseData();

        TpcHNormalizedModelResource tpcHNormalizedQueries = new TpcHNormalizedModelResource(
                mongoClient);

        TpcHMixedModelResource tpcHMixedModelResource = new TpcHMixedModelResource(
                mongoClient);

        TpcHDenormalizedModelResource tpcHDenormalizedModelResource = new TpcHDenormalizedModelResource(
                mongoClient);

        final MongoHealthCheck healthCheck = new MongoHealthCheck(mongoClient);
        environment.healthChecks().register("mongodb", healthCheck);
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(tpcHNormalizedQueries);
        environment.jersey().register(tpcHMixedModelResource);
        environment.jersey().register(tpcHDenormalizedModelResource);

    }

}
