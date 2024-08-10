package com.yatra.payment.ui.display.beans;

public class CashPoolPaymentOption extends PaymentOption {
	private String userBalance;
	
	public CashPoolPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
	}

	public String getUserBalance() {
		return userBalance;
	}

	public void setUserBalance(String userBalance) {
		this.userBalance = userBalance;
	}
}
