package org.redis.session.resources;

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

import org.redis.session.Session;

import redis.clients.jedis.Jedis;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/session", description = "Operations for session management")
public class SessionResource {

	private static final String CLASS_NAME = SessionResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;
	private Integer sessionValidityTime;

	public SessionResource(Jedis jedisClient, Integer sessionValidityTime) {
		this.jedisClient = jedisClient;
		this.sessionValidityTime = sessionValidityTime;
	}

	@POST
	@Path("/")
	@Timed
	@ApiOperation(value = "add new session", notes = "Returns the added session", response = Session.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Session details wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't add from Redis !") })
	public Session addSession(
			@ApiParam(value = "Session details", required = true) Session session) {

		if (session == null || session.getToken() == null
				|| session.getData() == null) {
			final String shortReason = "Session details wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			long timestamp = System.currentTimeMillis() / 1000;
			// add the session
			this.jedisClient.set("login:" + session.getToken(),
					session.getData());
			// add it to the recent sorted set with timestamp as score
			this.jedisClient.zadd("recent:", timestamp, session.getToken());
			// expire session each 10 hours
			this.jedisClient.expire("login:" + session.getToken(),
					this.sessionValidityTime);
			// keep only the top 100 recent session
			this.jedisClient.zremrangeByRank("recent:", 0, -101);
			return session;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"coudln't add from Redis" + e.getLocalizedMessage());
			final String shortReason = "coudln't add from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/{token}")
	@Timed
	@ApiOperation(value = "get a session", notes = "Returns a session by token", response = Session.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "token wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't get from Redis !") })
	public Session getSession(
			@ApiParam(value = "token", required = true) @PathParam("token") String token) {

		if (token == null) {
			final String shortReason = "token wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			// get the session by token
			Session result = new Session();
			result.setData(this.jedisClient.get("login:" + token));
			result.setToken(token);

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

	@PUT
	@Path("/")
	@Timed
	@ApiOperation(value = "update a session", notes = "Returns a the updated session", response = Session.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Session details wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't add from Redis !") })
	public Session updateSession(
			@ApiParam(value = "Session details", required = true) Session session) {

		if (session == null || session.getToken() == null
				|| session.getData() == null) {
			final String shortReason = "Session details wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			long timestamp = System.currentTimeMillis() / 1000;
			// update it, will overwrite if exists
			this.jedisClient.set("login:" + session.getToken(),
					session.getData());
			// add it to the recent sorted set with timestamp as score
			this.jedisClient.zadd("recent:", timestamp, session.getToken());

			// expire session each 10 hours
			this.jedisClient.expire("login:" + session.getToken(),
					this.sessionValidityTime);
			// keep only the top 100 recent session
			this.jedisClient.zremrangeByRank("recent:", 0, -101);
			return session;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"coudln't add from Redis" + e.getLocalizedMessage());
			final String shortReason = "coudln't add from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	@Path("/{token}")
	@Timed
	@ApiOperation(value = "delete a session", notes = "Returns true or false", response = Boolean.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "token wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't delete from Redis !") })
	public Boolean deleteSession(
			@ApiParam(value = "token", required = true) @PathParam("token") String token) {

		if (token == null) {
			final String shortReason = "token wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			// delete the session
			this.jedisClient.del("login:" + token);
			this.jedisClient.zrem("recent:", token);
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"coudln't get from Redis" + e.getLocalizedMessage());
			final String shortReason = "coudln't get from Redis !";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/recent100")
	@Timed
	@ApiOperation(value = "get recent 100 sessions", notes = "Returns list of sessions", response = Session.class, responseContainer = "list")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "coudln't get from Redis !") })
	public ArrayList<Session> getRecent100Sessions() {

		try {
			ArrayList<Session> result = new ArrayList<Session>();

			// get all recent tokens from the recent sorted set
			long sortedSetSize = this.jedisClient.zcard("recent:");
			Set<String> allRecentSessions = this.jedisClient.zrange("recent:",
					0, sortedSetSize - 1);

			String[] sessionTokens = allRecentSessions
					.toArray(new String[allRecentSessions.size()]);
			for (String token : sessionTokens) {
				Session session = new Session();
				session.setToken(token);
				session.setData(this.jedisClient.get("login:" + token));
				result.add(session);
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

}
