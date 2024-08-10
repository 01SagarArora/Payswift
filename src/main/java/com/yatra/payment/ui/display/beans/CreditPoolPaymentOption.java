package com.yatra.payment.ui.display.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CreditPoolPaymentOption extends PaymentOption{
	private String poolType;
	private String cashAmount;
	private String creditAmount;
	private String showBalance;
	
	public CreditPoolPaymentOption() {}
	
	public CreditPoolPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof CreditPoolPaymentOption) {
			CreditPoolPaymentOption creditPoolPaymentOption = (CreditPoolPaymentOption) paymentOption;
			this.cashAmount = creditPoolPaymentOption.getCashAmount();
			this.creditAmount = creditPoolPaymentOption.getCreditAmount();	
			this.showBalance = creditPoolPaymentOption.getShowBalance();
		}
	}
	

	public String getCashAmount() {
		return cashAmount;
	}

	public void setCashAmount(String cashAmount) {
		this.cashAmount = cashAmount;
	}

	public String getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(String creditAmount) {
		this.creditAmount = creditAmount;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getPoolType() {
		return poolType;
	}

	public void setPoolType(String poolType) {
		this.poolType = poolType;
	}
	
	public String getShowBalance() {
		return showBalance;
	}

	public void setShowBalance(String showBalance) {
		this.showBalance = showBalance;
	}
}
