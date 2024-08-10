package com.yatra.payment.ui.display.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

public class IVRPaymentOption extends PaymentOption {

	private String cardHolderDetailsLabel;
	private String cardHolderNameLabel;
	private String cardHolderMobileLabel;
	private String cardHolderEmailLabel;
	
	public IVRPaymentOption() {}
	
	public IVRPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof IVRPaymentOption) {
			IVRPaymentOption ivrPaymentOption = (IVRPaymentOption) paymentOption;
			this.cardHolderDetailsLabel = ivrPaymentOption.getCardHolderDetailsLabel();
			this.cardHolderNameLabel = ivrPaymentOption.getCardHolderNameLabel();
			this.cardHolderMobileLabel = ivrPaymentOption.getCardHolderMobileLabel();
			this.cardHolderEmailLabel = ivrPaymentOption.getCardHolderEmailLabel();
		}
	}
	
	public String getCardHolderDetailsLabel() {
		return cardHolderDetailsLabel;
	}

	public void setCardHolderDetailsLabel(String cardHolderDetailsLabel) {
		this.cardHolderDetailsLabel = cardHolderDetailsLabel;
	}

	public String getCardHolderNameLabel() {
		return cardHolderNameLabel;
	}

	public void setCardHolderNameLabel(String cardHolderNameLabel) {
		this.cardHolderNameLabel = cardHolderNameLabel;
	}

	public String getCardHolderMobileLabel() {
		return cardHolderMobileLabel;
	}

	public void setCardHolderMobileLabel(String cardHolderMobileLabel) {
		this.cardHolderMobileLabel = cardHolderMobileLabel;
	}

	public String getCardHolderEmailLabel() {
		return cardHolderEmailLabel;
	}

	public void setCardHolderEmailLabel(String cardHolderEmailLabel) {
		this.cardHolderEmailLabel = cardHolderEmailLabel;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
