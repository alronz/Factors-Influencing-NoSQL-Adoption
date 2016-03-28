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

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/inventory", description = "add/buy a product")
public class InventoryResource {

	private static final String CLASS_NAME = InventoryResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;

	public InventoryResource(Jedis jedisClient) {
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
				+ executedCount + " and number of errored out commands are:" + notExecutedCount)
				.toString();
	}

	@GET
	@Path("/buy/{sku}/seller/{sellerID}/token/{token}")
	@Timed
	@ApiOperation(value = "buy a product from seller ", notes = "Returns execution summery", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Coudln't access Redis "),
			@ApiResponse(code = 400, message = "sku or sellerID wasn't given! "),
			@ApiResponse(code = 400, message = "seller inventory is empty ") })
	public String buyProduct(
			@ApiParam(value = "product sku", required = true) @PathParam("sku") String sku,
			@ApiParam(value = "seller id", required = true) @PathParam("sellerID") String sellerID,
			@ApiParam(value = "token", required = true) @PathParam("token") String token) {

		if (sku == null || sellerID == null) {
			final String shortReason = "sku or sellerID wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			ArrayList<Response<Long>> responses = new ArrayList<Response<Long>>();

			this.jedisClient.watch("inventory:" + sellerID);
			if (!this.jedisClient.sismember("inventory:" + sellerID, sku)) {
				this.jedisClient.unwatch();

				final String shortReason = "seller inventory is empty";
				Exception cause = new IllegalArgumentException(shortReason);
				throw new WebApplicationException(cause,
						javax.ws.rs.core.Response.Status.BAD_REQUEST);

			}

			Transaction transaction = this.jedisClient.multi();

			responses.add(transaction.srem("inventory:" + sellerID, sku));
			responses.add(transaction.sadd("bought:" + token, sku));

			transaction.exec();

			return checkReponses(responses);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis " + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis ";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Path("/addItem/{sku}/seller/{sellerID}")
	@Timed
	@ApiOperation(value = "buy a product from seller ", notes = "Returns execution summery", response = Boolean.class)
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Coudln't access Redis "),
			@ApiResponse(code = 400, message = "sku or sellerID wasn't given! ") })
	public boolean addProduct(
			@ApiParam(value = "product sku", required = true) @PathParam("sku") String sku,
			@ApiParam(value = "seller id", required = true) @PathParam("sellerID") String sellerID) {

		if (sku == null || sellerID == null) {
			final String shortReason = "sku or sellerID wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			this.jedisClient.sadd("inventory:" + sellerID, sku);

			return true;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis " + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis ";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/sellerProducts/{sellerID}")
	@Timed
	@ApiOperation(value = "get seller products ", notes = "Returns seller products", response = String.class, responseContainer = "set")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Coudln't access Redis "),
			@ApiResponse(code = 400, message = "sellerID wasn't given! ") })
	public Set<String> getSellerProducts(
			@ApiParam(value = "seller id", required = true) @PathParam("sellerID") String sellerID) {

		if (sellerID == null) {
			final String shortReason = "sellerID wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			Set<String> listOfSkus = this.jedisClient.smembers("inventory:"
					+ sellerID);

			return listOfSkus;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis " + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis ";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/boughtProducts/{token}")
	@Timed
	@ApiOperation(value = "get bought products ", notes = "Returns bought products", response = String.class, responseContainer = "set")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Coudln't access Redis "),
			@ApiResponse(code = 400, message = "token wasn't given! ") })
	public Set<String> getBoughtProducts(
			@ApiParam(value = "token", required = true) @PathParam("token") String token) {

		if (token == null) {
			final String shortReason = "token wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			Set<String> listOfSkus = this.jedisClient.smembers("bought:"
					+ token);

			return listOfSkus;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis " + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis ";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
