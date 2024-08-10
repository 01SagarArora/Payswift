package com.yatra.payment.ui.display.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EWalletPaymentOption extends PaymentOption {
	
	private String walletName;
	private String walletCode;
	private String logoURL;
	
	public EWalletPaymentOption() {}
	
	public EWalletPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof EWalletPaymentOption) {
			EWalletPaymentOption eWalletPaymentOption = (EWalletPaymentOption) paymentOption;
			this.walletName = eWalletPaymentOption.getWalletName();
			this.walletCode = eWalletPaymentOption.getWalletCode();
			this.logoURL = eWalletPaymentOption.getLogoURL();
		}
	}
	
	public String getWalletName() {
		return walletName;
	}

	public void setWalletName(String walletName) {
		this.walletName = walletName;
	}

	public String getWalletCode() {
		return walletCode;
	}

	public void setWalletCode(String walletCode) {
		this.walletCode = walletCode;
	}

	public String getLogoURL() {
		return logoURL;
	}

	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
