package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EMIPaymentOption extends PaymentOption {

	private List<EMIBank> banks;
	private String cardHolderNameLabel;
	private String cardNoLabel;

	public EMIPaymentOption() {}

	public EMIPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof EMIPaymentOption) {
			EMIPaymentOption emiPaymentOption = (EMIPaymentOption) paymentOption;
			this.banks = emiPaymentOption.getBanks();
			this.cardHolderNameLabel = emiPaymentOption.getCardHolderNameLabel();
			this.cardNoLabel = emiPaymentOption.getCardNoLabel();
		}
	}

	public List<EMIBank> getBanks() {
		return banks;
	}

	public void setBanks(List<EMIBank> banks) {
		this.banks = banks;
	}

	public String getCardHolderNameLabel() {
		return cardHolderNameLabel;
	}

	public void setCardHolderNameLabel(String cardHolderNameLabel) {
		this.cardHolderNameLabel = cardHolderNameLabel;
	}

	public String getCardNoLabel() {
		return cardNoLabel;
	}

	public void setCardNoLabel(String cardNoLabel) {
		this.cardNoLabel = cardNoLabel;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
