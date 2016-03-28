package org.redis.cart.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.redis.cart.CartItem;

import redis.clients.jedis.Jedis;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.*;

@Path("/cart")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/cart", description = "Operations for cart management")
public class CartResource {

	private static final String CLASS_NAME = CartResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;
	private Integer cartValidityTime;

	public CartResource(Jedis jedisClient, Integer cartValidityTime) {
		this.jedisClient = jedisClient;
		this.cartValidityTime = cartValidityTime;
	}

	@POST
	@Path("/add/{session}")
	@Timed
	@ApiOperation(value = "add new cart item", notes = "Returns the added Cart Item", response = CartItem.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Cart item details or session wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't add or delete from Redis !") })
	public CartItem addCartItem(
			@ApiParam(value = "Session id", required = true) @PathParam("session") String session,
			@ApiParam(value = "Cart Item", required = true) CartItem cartItem) {

		if (session == null || cartItem.getSku() == null
				|| cartItem.getPrice() == null || cartItem.getAmount() == null) {
			final String shortReason = "Cart item details or session wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			if (cartItem.getAmount() == 0) {
				jedisClient.hdel("cart:" + session, cartItem.getSku()
						.toString());
			} else {
				jedisClient.hset("cart:" + session, cartItem.getSku()
						.toString(), cartItem.toJson());
				jedisClient.expire("cart:" + session, this.cartValidityTime);
			}

			return cartItem;
		} catch (Exception e) {
			LOGGER.log(
					Level.SEVERE,
					"coudln't add or delete from Redis"
							+ e.getLocalizedMessage());
			final String shortReason = "coudln't add or delete from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	@Path("/delete/{session}")
	@Timed
	@ApiOperation(value = "delete cart item from cart", notes = "Returns the deleted Cart Item", response = CartItem.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Cart item details or session wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't add or delete from Redis !") })
	public CartItem deleteCartItem(
			@ApiParam(value = "Session id", required = true) @PathParam("session") String session,
			@ApiParam(value = "Cart Item", required = true) CartItem cartItem) {

		if (session == null || cartItem.getSku() == null) {
			final String shortReason = "Cart item details or session wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {

			jedisClient.hdel("cart:" + session, cartItem.getSku().toString());

			return cartItem;
		} catch (Exception e) {
			LOGGER.log(
					Level.SEVERE,
					"coudln't add or delete from Redis"
							+ e.getLocalizedMessage());
			final String shortReason = "coudln't add or delete from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/{session}")
	@Timed
	@ApiOperation(value = "get cart", notes = "Returns the cart details", response = CartItem.class, responseContainer = "list")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "session wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't get from Redis !") })
	public ArrayList<CartItem> getCart(
			@ApiParam(value = "Session id", required = true) @PathParam("session") String session) {

		if (session == null) {
			final String shortReason = "session wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}
		ArrayList<CartItem> result = new ArrayList<CartItem>();

		try {

			Map<String, String> restul = jedisClient.hgetAll("cart:" + session);
			Iterator it = restul.entrySet().iterator();
			while (it.hasNext()) {
				CartItem cart = new CartItem();
				Map.Entry pair = (Map.Entry) it.next();
				JsonObject jObject = new JsonParser().parse(
						pair.getValue().toString()).getAsJsonObject();
				cart.setSku(jObject.get("sku").toString());
				cart.setAmount(jObject.get("amount").getAsDouble());
				cart.setPrice(jObject.get("price").getAsDouble());
				result.add(cart);
				it.remove(); // avoids a ConcurrentModificationException
			}

			return result;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"coudln't get from Redis" + e.getLocalizedMessage());
			final String shortReason = "coudln't get from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	@Path("/{session}")
	@Timed
	@ApiOperation(value = "delete cart", notes = "Returns true or false", response = Boolean.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "session wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't delete from Redis !") })
	public boolean deleteCart(
			@ApiParam(value = "Session id", required = true) @PathParam("session") String session) {

		if (session == null) {
			final String shortReason = "session wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {

			this.jedisClient.expire("cart:" + session, 0);

			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"coudln't delete from Redis" + e.getLocalizedMessage());
			final String shortReason = "coudln't delete from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
