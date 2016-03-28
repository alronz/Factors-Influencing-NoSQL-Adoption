package org.redis.session;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {

	private String token;

	// This can be user data , products he viewed , or whatever
	private String data;

	@JsonProperty
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@JsonProperty
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Session [token=" + token + ", data=" + data + "]";
	}

	public String toJson() {
		return "{\"token\":\"" + token + "\", \"data\":\"" + data + "\"}";
	}

}
