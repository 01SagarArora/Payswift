package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ATMCardPaymentOption extends PaymentOption {

	private List<Bank> banks;
	
	public ATMCardPaymentOption() {}
	
	public ATMCardPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof ATMCardPaymentOption) {
			ATMCardPaymentOption atmCardPaymentOption = (ATMCardPaymentOption) paymentOption;
			this.banks = atmCardPaymentOption.getBanks();
		}
	}
	
	public List<Bank> getBanks() {
		return banks;
	}

	public void setBanks(List<Bank> banks) {
		this.banks = banks;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
