package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;

public class MobileWalletPaymentOption extends PaymentOption {
	
	private String walletName;
	private String walletCode;
	private String logoURL;
	private List<UIPayopSuboptionMapping> suboptionList;
	
	
	

	public MobileWalletPaymentOption() {}
	
	public MobileWalletPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof MobileWalletPaymentOption) {
			MobileWalletPaymentOption mobileWalletPaymentOption = (MobileWalletPaymentOption) paymentOption;
			this.walletName = mobileWalletPaymentOption.getWalletName();
			this.walletCode = mobileWalletPaymentOption.getWalletCode();
			this.logoURL = mobileWalletPaymentOption.getLogoURL();
			this.suboptionList = mobileWalletPaymentOption.getSuboptionList();
			
		}
	}
	
	public String getWalletName() {
		return walletName;
	}
	
	public List<UIPayopSuboptionMapping> getSuboptionList() {
		return suboptionList;
	}

	public void setSuboptionList(List<UIPayopSuboptionMapping> suboptionList) {
		this.suboptionList = suboptionList;
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
