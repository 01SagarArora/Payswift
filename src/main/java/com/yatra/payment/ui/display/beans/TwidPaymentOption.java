package com.yatra.payment.ui.display.beans;

public class TwidPaymentOption extends PaymentOption {
	
	private String subText;
	private String balance;
	
	public TwidPaymentOption() {}

	public TwidPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
	}

	public String getSubText() {
		return subText;
	}

	public void setSubText(String subText) {
		this.subText = subText;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}
}
