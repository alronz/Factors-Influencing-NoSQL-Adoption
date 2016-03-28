package org.redis.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItem {

	private String sku;
	private Double amount;
	private Double price;

	@JsonProperty
	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	@JsonProperty
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@JsonProperty
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "CartItem [sku=" + sku + ", amount=" + amount + ", price="
				+ price + "]";
	}

	public String toJson() {
		return "{\"sku\":\"" + sku + "\", \"amount\":\"" + amount
				+ "\", \"price\":\"" + price + "\"}";
	}

}
