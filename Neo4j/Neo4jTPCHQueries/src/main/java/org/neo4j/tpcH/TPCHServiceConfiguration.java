package org.neo4j.tpcH;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class TPCHServiceConfiguration extends Configuration {

	
	private String neo4jPath;
	
	
	

	public String getNeo4jPath() {
		return neo4jPath;
	}




	public void setNeo4jPath(String neo4jPath) {
		this.neo4jPath = neo4jPath;
	}




	public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
		return swaggerBundleConfiguration;
	}




	public void setSwaggerBundleConfiguration(
			SwaggerBundleConfiguration swaggerBundleConfiguration) {
		this.swaggerBundleConfiguration = swaggerBundleConfiguration;
	}




	@JsonProperty("swagger")
	public SwaggerBundleConfiguration swaggerBundleConfiguration;

}
