package com.yatra.payment.ui.display.beans;

import java.util.List;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;

public class CorporateCardPaymentOption extends PaymentOption{

	private List<CorporateCardInfo> corporateCards;

	public CorporateCardPaymentOption(){}
	
	public CorporateCardPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof CorporateCardPaymentOption) {
			CorporateCardPaymentOption corporateCardPaymentOption = (CorporateCardPaymentOption) paymentOption;
			this.corporateCards = corporateCardPaymentOption.getCorporateCards();
		}
	}

	public List<CorporateCardInfo> getCorporateCards() {
		return corporateCards;
	}

	public void setCorporateCards(List<CorporateCardInfo> corporateCards) {
		this.corporateCards = corporateCards;
	}
	
}
