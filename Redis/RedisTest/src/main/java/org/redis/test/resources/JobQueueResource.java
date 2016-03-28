package org.redis.test.resources;

import java.util.ArrayList;
import java.util.List;
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

import org.redis.test.Job;

import redis.clients.jedis.Jedis;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/JobQueue")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/JobQueue", description = "Operations for job queue management")
public class JobQueueResource {

	private static final String CLASS_NAME = JobQueueResource.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private Jedis jedisClient = null;

	public JobQueueResource(Jedis jedisClient) {
		this.jedisClient = jedisClient;
	}

	@POST
	@Path("/enqueue")
	@Timed
	@ApiOperation(value = "enqueue a job ", notes = "Returns the enqueued job", response = Job.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "job wasn't given!"),
			@ApiResponse(code = 500, message = "Coudln't access Redis") })
	public Job enqueue(
			@ApiParam(value = "job to be queued", required = true) Job job) {

		if (job == null || job.getQueueName() == null || job.getData() == null) {
			final String shortReason = "job wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {
			// enqueue the job inside the queue list
			this.jedisClient
					.rpush("queue:" + job.getQueueName(), job.getData());
			// add the queue name to a set
			this.jedisClient.sadd("queues:", job.getQueueName());

			return job;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis" + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/dequeue/{queueName}/type/{queueType}")
	@Timed
	@ApiOperation(value = "dequeue a job ", notes = "Returns the dequeued job", response = Job.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "queue Name or type wasn't given!"),
			@ApiResponse(code = 500, message = "Coudln't access Redis"),
			@ApiResponse(code = 400, message = "queue type not supported !") })
	public Job dequeue(
			@ApiParam(value = "queue Name to be dequeued", required = true) @PathParam("queueName") String queueName,
			@ApiParam(value = "queue type,FIFO or LIFO", required = true) @PathParam("queueType") String queueType) {

		if (queueName == null || queueType == null) {
			final String shortReason = "queue Name or type wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {

			Job result = new Job();
			result.setQueueName(queueName);
			if (queueType.equals("FIFO")) {
				// dequeue the job from the queue list
				result.setData(this.jedisClient.lpop("queue:" + queueName));
			} else if (queueType.equals("LIFO")) {
				// dequeue the job from the queue list
				result.setData(this.jedisClient.rpop("queue:" + queueName));
			} else {
				final String shortReason = "queue type not supported !";
				Exception cause = new IllegalArgumentException(shortReason);
				throw new WebApplicationException(cause,
						javax.ws.rs.core.Response.Status.BAD_REQUEST);
			}

			// check if the list is empty then remove it from queues set
			if (this.jedisClient.llen("queue:" + queueName) == 0) {
				this.jedisClient.srem("queues:", queueName);
			}

			return result;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis" + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/peak/{queueName}/number/{elelementsNumber}")
	@Timed
	@ApiOperation(value = "peak elements from head ", notes = "Returns some elements from head", response = String.class, responseContainer = "list")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "queue name or elelemnts number wasn't given!"),
			@ApiResponse(code = 500, message = "Coudln't access Redis") })
	public List<String> peakHead(
			@ApiParam(value = "queue Name", required = true) @PathParam("queueName") String queueName,
			@ApiParam(value = "elelments number to return", required = true) @PathParam("elelementsNumber") int elelementsNumber) {

		if (queueName == null || elelementsNumber == 0) {
			final String shortReason = "queue name or elelemnts number wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {

			List<String> results = this.jedisClient.lrange(
					"queue:" + queueName, 0, elelementsNumber);

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

	@DELETE
	@Path("/DeleteJob")
	@Timed
	@ApiOperation(value = "delete element from a queue ", notes = "Returns the deleted element", response = Job.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "job wasn't given!"),
			@ApiResponse(code = 500, message = "Coudln't access Redis") })
	public Job deleteElement(
			@ApiParam(value = "job to be deleted", required = true) Job job) {

		if (job == null || job.getQueueName() == null || job.getData() == null) {
			final String shortReason = "job wasn't given!";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.BAD_REQUEST);
		}

		try {

			this.jedisClient.lrem("queue:" + job.getQueueName(), 0,
					job.getData());

			// check if the list is empty then remove it from queues set
			if (this.jedisClient.llen("queue:" + job.getQueueName()) == 0) {
				this.jedisClient.srem("queues:", job.getQueueName());
			}

			return job;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Coudln't access Redis" + e.getLocalizedMessage());
			final String shortReason = "Coudln't access Redis";
			Exception cause = new IllegalArgumentException(shortReason);
			throw new WebApplicationException(cause,
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/queues")
	@Timed
	@ApiOperation(value = "get all queue names", notes = "Returns the name of all queues", response = String.class, responseContainer = "set")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Coudln't access Redis") })
	public Set<String> allQueues() {

		try {
			Set<String> results = this.jedisClient.smembers("queues:");

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
