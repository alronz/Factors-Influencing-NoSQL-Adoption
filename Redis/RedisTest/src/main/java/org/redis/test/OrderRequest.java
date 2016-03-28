package org.redis.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRequest {

	private Order order;

	private Shipping shipping;

	private Payment payment;

	@JsonProperty
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	@JsonProperty
	public Shipping getShipping() {
		return shipping;
	}

	public void setShipping(Shipping shipping) {
		this.shipping = shipping;
	}

	@JsonProperty
	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

}
