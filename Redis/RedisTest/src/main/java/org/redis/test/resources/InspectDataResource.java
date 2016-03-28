package org.redis.test.resources;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
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

@Path("/inspect")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/inspect", description = "get information about current data structures")
public class InspectDataResource {

	private static final String CLASS_NAME = InspectDataResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;

	public InspectDataResource(Jedis jedisClient) {
		this.jedisClient = jedisClient;
	}

	@GET
	@Path("/getAllKeys/pattern/{pattern}")
	@Timed
	@ApiOperation(value = "get all keys ", notes = "Returns all key names", response = String.class, responseContainer = "set")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "pattern isn't given"),
			@ApiResponse(code = 500, message = "Coudln't access Redis") })
	public Set<String> getAllKeys(
			@ApiParam(value = "key patterns", required = true) @PathParam("pattern") String pattern) {

		if (pattern == null) {
			final String shortReason = "pattern isn't given";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {

			Set<String> results = this.jedisClient.keys(pattern);

			return results;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis" + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

}
