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
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/vip")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/vip", description = "add current logged in users to VIP")
public class VIPResource {

	private static final String CLASS_NAME = VIPResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;

	public VIPResource(Jedis jedisClient) {
		this.jedisClient = jedisClient;
	}

	private String checkReponses(ArrayList<Response<Long>> responses) {
		int notExecutedCount = 0;
		int executedCount = 0;
		int totalCount = 0;
		for (Response<Long> response : responses) {
			totalCount++;
			if (response.get() == 0) {
				notExecutedCount++;
			} else if (response.get() == 1) {
				executedCount++;
			} else {
				LOGGER.info("response is" + response.get());
			}
		}
		LOGGER.info("out of:" + totalCount
				+ " commands, number of successfully executed commands are:"
				+ executedCount + " and number of errored out commands are:"
				+ notExecutedCount);

		return ("out of:" + totalCount
				+ " commands, number of successfully executed commands are:"
				+ executedCount + " and number of errored out commands are:" + notExecutedCount).toString();
	}

	@GET
	@Path("/addToVIP")
	@Timed
	@ApiOperation(value = "add All current Logged In Customers to VIP ", notes = "Returns execution summery", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Coudln't access Redis") })
	public String addToVIP() {

		try {
			
			long sortedSetSize = this.jedisClient.zcard("login:");
			Set<String> allLoggedInUsers = this.jedisClient.zrange("login:", 0,
					sortedSetSize - 1);

			String[] tokenList = allLoggedInUsers
					.toArray(new String[allLoggedInUsers.size()]);
			Pipeline pipe = this.jedisClient.pipelined();
			ArrayList<Response<Long>> responses = new ArrayList<Response<Long>>();
			for (String token : tokenList) {
				responses.add(pipe.sadd("VIP:", token));
			}
			pipe.sync();

			return checkReponses(responses);

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
