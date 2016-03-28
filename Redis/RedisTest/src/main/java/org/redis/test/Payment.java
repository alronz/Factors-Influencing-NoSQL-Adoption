package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payment {

	private String paymentID;

	private String otherPaymentDetails;

	@JsonProperty
	public String getPaymentID() {
		return paymentID;
	}

	public void setPaymentID(String paymentID) {
		this.paymentID = paymentID;
	}

	@JsonProperty
	public String getOtherPaymentDetails() {
		return otherPaymentDetails;
	}

	public void setOtherPaymentDetails(String otherPaymentDetails) {
		this.otherPaymentDetails = otherPaymentDetails;
	}

}
