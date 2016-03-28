package org.redis.cache.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.redis.cache.Request;

import redis.clients.jedis.Jedis;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/cache", description = "Operations for cache management")
public class CacheResource {

	private static final String CLASS_NAME = CacheResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;
	private Integer cacheValidityTime;

	public CacheResource(Jedis jedisClient, Integer cacheValidityTime) {
		this.jedisClient = jedisClient;
		this.cacheValidityTime = cacheValidityTime;
	}

	private String getPageContent(String url) {
		String result = "Empty content";

		// do some logic here to get the request content from web server by url
		// and return it as String or whatever format

		return result;
	}

	@GET
	@Path("/{pageRequestURL}")
	@Timed
	@ApiOperation(value = "get a cached request", notes = "Returns a cached request by url", response = Request.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "pageRequestURL wasn't given!"),
			@ApiResponse(code = 500, message = "coudln't get from Redis !") })
	public Request getRequest(
			@ApiParam(value = "pageRequestURL", required = true) @PathParam("pageRequestURL") String pageRequestURL) {

		if (pageRequestURL == null) {
			final String shortReason = "pageRequestURL wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			// get the cached request page by url
			Request resultRequest = new Request();
			resultRequest.setPageRequestURL(pageRequestURL);

			String cachedPageKey = "cache:" + pageRequestURL.hashCode();
			// check if the request already cached
			String chachedPageContent = this.jedisClient.get(cachedPageKey);

			// if cache miss
			if (chachedPageContent == null) {
				String pageContent = getPageContent(pageRequestURL);
				this.jedisClient.setex(cachedPageKey, this.cacheValidityTime,
						pageContent);
				resultRequest.setPageContent(pageContent);
			} else // cache hit
			{
				resultRequest.setPageContent(chachedPageContent);
			}

			return resultRequest;
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
