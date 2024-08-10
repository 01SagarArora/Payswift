package com.yatra.payment.client.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class CardHolderDetails {
	
	private final String cardHolderName;
	private final String cardNumber;
	private final String cardExpiryYear;
	private final String cardExpiryMonth;
	private final String cardType;
	private final String cardCVV;
	private final boolean isCardInternational;
	private final CardHolderAddress cardHolderAddress;
	
	public CardHolderDetails(String cardHolderName, String cardNumber, String cardExpiryYear, String cardExpiryMonth, String cardType, String cardCVV, CardHolderAddress cardHolderAddress, boolean isCardInternational) {
		this.cardHolderName = cardHolderName;
		this.cardNumber = cardNumber;
		this.cardExpiryYear = cardExpiryYear;
		this.cardExpiryMonth = cardExpiryMonth;
		this.cardType = cardType;
		this.cardCVV = cardCVV;
		this.cardHolderAddress = cardHolderAddress;
		this.isCardInternational = isCardInternational;
	}
	
	public String getCardHolderName() {
		return cardHolderName;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public String getCardExpiryYear() {
		return cardExpiryYear;
	}
	public String getCardExpiryMonth() {
		return cardExpiryMonth;
	}
	public String getCardType() {
		return cardType;
	}
	public String getCardCVV() {
		return cardCVV;
	}
	public boolean isCardInternational() {
		return isCardInternational;
	}
	public CardHolderAddress getCardHolderAddress() {
		return cardHolderAddress;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
