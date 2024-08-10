package com.yatra.payment.ui.display.beans;

public class EmiType {

	private String emiTenure;
	private String interestRate;
	private String minAmount;
	private String maxAmount;
	
	public EmiType() {
	}
	
	public EmiType(String emiTenure, String interestRate, String minAmount, String maxAmount) {
		this.emiTenure = emiTenure;
		this.interestRate = interestRate;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}
	
	
	public String getEmiTenure() {
		return emiTenure;
	}
	public void setEmiTenure(String emiTenure) {
		this.emiTenure = emiTenure;
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
}
