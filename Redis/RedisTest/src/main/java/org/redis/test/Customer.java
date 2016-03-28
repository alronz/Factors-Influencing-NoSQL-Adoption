package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Customer {

	private String customerID;

	private String otherCustomerDetails;

	@JsonProperty
	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	@JsonProperty
	public String getOtherCustomerDetails() {
		return otherCustomerDetails;
	}

	public void setOtherCustomerDetails(String otherCustomerDetails) {
		this.otherCustomerDetails = otherCustomerDetails;
	}

}
