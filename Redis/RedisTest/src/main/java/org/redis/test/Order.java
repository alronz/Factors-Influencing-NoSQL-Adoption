package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

	private String orderID;

	private String otherOrderDetails;

	@JsonProperty
	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	@JsonProperty
	public String getOtherOrderDetails() {
		return otherOrderDetails;
	}

	public void setOtherOrderDetails(String otherOrderDetails) {
		this.otherOrderDetails = otherOrderDetails;
	}

}
