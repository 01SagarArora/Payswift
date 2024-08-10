package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CashCardPaymentOption extends PaymentOption {
	
	private List<CashCard> cashCards;

	public CashCardPaymentOption() {}
	
	public CashCardPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof CashCardPaymentOption) {
			CashCardPaymentOption cashCardPaymentOption = (CashCardPaymentOption) paymentOption;
			this.cashCards = cashCardPaymentOption.getCashCards();
		}
	}

	public List<CashCard> getCashCards() {
		return cashCards;
	}

	public void setCashCards(List<CashCard> cashCards) {
		this.cashCards = cashCards;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
