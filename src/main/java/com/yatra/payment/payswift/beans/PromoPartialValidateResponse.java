package com.yatra.payment.payswift.beans;

public class PromoPartialValidateResponse {
	private boolean success;
	private String promoCode;
	private String amount;
	private String message;
	public boolean isSuccess() {
		return success;
	}
	public String getPromoCode() {
		return promoCode;
	}
	public String getAmount() {
		return amount;
	}
	public String getMessage() {
		return message;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
