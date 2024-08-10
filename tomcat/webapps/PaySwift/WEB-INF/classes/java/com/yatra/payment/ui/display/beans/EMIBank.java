package com.yatra.payment.ui.display.beans;

import java.util.List;

import com.yatra.payment.ui.enums.Status;

public class EMIBank {
	
	private String displayText;
	private String code;
	private Status status;
	private String transactionFee;
	private String logoURL;
	private List<CardType> cardTypes;
	private List<EmiType> emiTypes;
	private String minAmount;
	private String maxAmount;
	private String displayMobile;
	
	public EMIBank() {}
	
	public EMIBank(EMIBank emiBank) {
		this.displayText = emiBank.getDisplayText();
		this.code = emiBank.getCode();
		this.status = emiBank.getStatus();
		this.transactionFee = emiBank.getTransactionFee();
		this.logoURL = emiBank.getLogoURL();
		this.cardTypes = emiBank.getCardTypes();
		this.emiTypes = emiBank.getEmiTypes();
		this.minAmount = emiBank.getMinAmount();
		this.maxAmount = emiBank.getMaxAmount();
		this.displayMobile = emiBank.getDisplayMobile();
	}
	
	public String getDisplayText() {
		return displayText;
	}
	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getTransactionFee() {
		return transactionFee;
	}
	public void setTransactionFee(String transactionFee) {
		this.transactionFee = transactionFee;
	}
	
	public String getLogoURL() {
		return logoURL;
	}
	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}
	public List<CardType> getCardTypes() {
		return cardTypes;
	}
	public void setCardTypes(List<CardType> cardTypes) {
		this.cardTypes = cardTypes;
	}
	
	@Override
	public String toString() {
		return "EMIBank [displayText=" + displayText + ", code=" + code
				 + ", status=" + status + ",transactionFee=" + transactionFee + ", logoURL=" + logoURL + ",cardTypes=" + cardTypes + ",maxAmount=" + maxAmount + ",minAmount=" + minAmount + ",displayMobile=" + displayMobile +"]";
	}

	public List<EmiType> getEmiTypes() {
		return emiTypes;
	}

	public void setEmiTypes(List<EmiType> emiTypes) {
		this.emiTypes = emiTypes;
	}
	
	public String getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(String minAmount) {
		this.minAmount = minAmount;
	}

	public String getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}
	
	public String getDisplayMobile() {
		return displayMobile;
	}

	public void setDisplayMobile(String displayMobile) {
		this.displayMobile = displayMobile;
	}

}
