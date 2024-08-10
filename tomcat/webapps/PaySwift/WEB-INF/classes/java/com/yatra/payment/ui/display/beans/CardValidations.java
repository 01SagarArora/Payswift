package com.yatra.payment.ui.display.beans;

public class CardValidations {
	private String validateLuhn;
	private String regex;
	private String cardNoLength;
	private String cvvLength;
	private String cvv;
	private String expiryDate;
	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getValidateLuhn() {
		return validateLuhn;
	}
	
	public void setValidateLuhn(String validateLuhn) {
		this.validateLuhn = validateLuhn;
	}
	
	public String getRegex() {
		return regex;
	}
	
	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	public String getCardNoLength() {
		return cardNoLength;
	}
	
	public void setCardNoLength(String cardNoLength) {
		this.cardNoLength = cardNoLength;
	}
	
	public String getCvvLength() {
		return cvvLength;
	}
	
	public void setCvvLength(String cvvLength) {
		this.cvvLength = cvvLength;
	}
	
	public String getCvv() {
		return cvv;
	}
	
	public void setCvv(String cvv) {
		this.cvv = cvv;
	}
	
	@Override
	public String toString() {
		return "CardValidations [validateLuhn=" + validateLuhn + ",regex=" + regex + ", cardNoLength=" + cardNoLength
				+ ", cvvLength=" + cvvLength + ", cvv=" + cvv + "]";
	}
}
