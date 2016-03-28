package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Job {

	private String queueName;

	private String data;

	@JsonProperty
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	@JsonProperty
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
