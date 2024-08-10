package com.yatra.payment.ui.display.beans;

public class BillingAddress {

	private String billingAddressLabel;
	private String billingAddress1;
	private String billingAddress2;
	private String billingCity;
	private String billingPin;
	private String billingState;
	private String billingCountry;
	private String billingIsdCode;
	private String billingMobile;

	public String getBillingAddressLabel() {
		return billingAddressLabel;
	}

	public void setBillingAddressLabel(String billingAddressLabel) {
		this.billingAddressLabel = billingAddressLabel;
	}

	public String getBillingAddress1() {
		return billingAddress1;
	}

	public void setBillingAddress1(String billingAddress1) {
		this.billingAddress1 = billingAddress1;
	}

	public String getBillingAddress2() {
		return billingAddress2;
	}

	public void setBillingAddress2(String billingAddress2) {
		this.billingAddress2 = billingAddress2;
	}

	public String getBillingCity() {
		return billingCity;
	}

	public void setBillingCity(String billingCity) {
		this.billingCity = billingCity;
	}

	public String getBillingPin() {
		return billingPin;
	}

	public void setBillingPin(String billingPin) {
		this.billingPin = billingPin;
	}

	public String getBillingState() {
		return billingState;
	}

	public void setBillingState(String billingState) {
		this.billingState = billingState;
	}

	public String getBillingCountry() {
		return billingCountry;
	}

	public void setBillingCountry(String billingCountry) {
		this.billingCountry = billingCountry;
	}

	public String getBillingIsdCode() {
		return billingIsdCode;
	}

	public void setBillingIsdCode(String billingIsdCode) {
		this.billingIsdCode = billingIsdCode;
	}

	public String getBillingMobile() {
		return billingMobile;
	}

	public void setBillingMobile(String billingMobile) {
		this.billingMobile = billingMobile;
	}

	@Override
	public String toString() {
		return "BillingAddress [billingAddressLabel=" + billingAddressLabel + ", billingAddress1=" + billingAddress1 + ", billingAddress2=" + billingAddress2
				+ ", billingCity=" + billingCity + ", billingPin=" + billingPin + ", billingState=" + billingState
				+ ", billingCountry=" + billingCountry + ", billingIsdCode=" + billingIsdCode + ", billingMobile="
				+ billingMobile + "]";
	}

}
