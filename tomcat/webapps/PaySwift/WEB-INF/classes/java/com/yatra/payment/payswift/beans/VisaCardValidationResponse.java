package com.yatra.payment.payswift.beans;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class VisaCardValidationResponse {

	private String status;
	private String amount;
	private String message;
	private String discountType;
	public String getStatus() {
		return status;
	}
	public void setStatus(String success) {
		this.status = success;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}
	
	
}
