package com.yatra.payment.ui.dao.bean;

import java.util.List;

public class UIEmiBanks {

	private String code;
	private String displayName;
	private String tenure;
	private String interestRate;
	private String transactionFee;
	private String imageUrl;
	private List<String> cardTypes;
	private String status;
	private String minAmount;
	private String maxAmount;
	private String displayMobile;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTenure() {
		return tenure;
	}

	public void setTenure(String tenure) {
		this.tenure = tenure;
	}

	public String getTransactionFee() {
		return transactionFee;
	}

	public void setTransactionFee(String transactionFee) {
		this.transactionFee = transactionFee;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public List<String> getCardTypes() {
		return cardTypes;
	}

	public void setCardTypes(List<String> cardTypes) {
		this.cardTypes = cardTypes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(String interestRate) {
		this.interestRate = interestRate;
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
