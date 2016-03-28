package org.redis.test.resources;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import redis.clients.jedis.Jedis;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/test", description = "test Redis features")
public class TestResource {

	private static final String CLASS_NAME = TestResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;

	public TestResource(Jedis jedisClient) {
		this.jedisClient = jedisClient;
	}

}
