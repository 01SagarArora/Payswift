package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DebitCardPaymentOption extends PaymentOption {

	private String cardHolderNameLabel;
	private String cardNoLabel;
	private List<CardType> cardTypes;
	private BillingAddress billingAddress;

	public DebitCardPaymentOption() {}
	
	public DebitCardPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof DebitCardPaymentOption) {
			DebitCardPaymentOption debitCardPaymentOption = (DebitCardPaymentOption) paymentOption;
			this.cardHolderNameLabel = debitCardPaymentOption.getCardHolderNameLabel();
			this.cardNoLabel = debitCardPaymentOption.getCardNoLabel();
			this.cardTypes = debitCardPaymentOption.getCardTypes();			
			this.billingAddress = debitCardPaymentOption.getBillingAddress();
		}
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

	public List<CardType> getCardTypes() {
		return cardTypes;
	}

	public void setCardTypes(List<CardType> cardTypes) {
		this.cardTypes = cardTypes;
	}
	
	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
