package com.yatra.payment.client.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class CardHolderAddress {
	
	private final String billingAddress;
	private final String billingCity;
	private final String billingState;
	private final String billingCountry;
	private final String billingPin;
	private final String billingISDCode;
	private final String billingMobile;
	
	public CardHolderAddress(String billingAddress, String billingCity, String billingState, String billingCountry, String billingPin, String billingISDCode, String billingMobile) {
		this.billingAddress = billingAddress;
		this.billingCity = billingCity;
		this.billingState = billingState;
		this.billingCountry = billingCountry;
		this.billingPin = billingPin;
		this.billingISDCode = billingISDCode;
		this.billingMobile = billingMobile;
	}
	
	public String getBillingAddress() {
		return billingAddress;
	}
	public String getBillingCity() {
		return billingCity;
	}
	public String getBillingState() {
		return billingState;
	}
	public String getBillingCountry() {
		return billingCountry;
	}
	public String getBillingPin() {
		return billingPin;
	}
	public String getBillingISDCode() {
		return billingISDCode;
	}
	public String getBillingMobile() {
		return billingMobile;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
