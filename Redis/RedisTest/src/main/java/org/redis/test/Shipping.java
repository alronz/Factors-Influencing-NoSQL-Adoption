package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Shipping {

	private String shippingID;

	private String otherShippingDetails;

	@JsonProperty
	public String getShippingID() {
		return shippingID;
	}

	public void setShippingID(String shippingID) {
		this.shippingID = shippingID;
	}

	@JsonProperty
	public String getOtherShippingDetails() {
		return otherShippingDetails;
	}

	public void setOtherShippingDetails(String otherShippingDetails) {
		this.otherShippingDetails = otherShippingDetails;
	}

}
