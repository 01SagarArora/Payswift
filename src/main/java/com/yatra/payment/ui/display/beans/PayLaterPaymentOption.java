package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;

public class PayLaterPaymentOption extends PaymentOption {

	private List<UIPayopSuboptionMapping> suboptionList;

	public PayLaterPaymentOption() {}
	
	public PayLaterPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof PayLaterPaymentOption) {
			PayLaterPaymentOption payLaterPaymentOption = (PayLaterPaymentOption) paymentOption;
			this.suboptionList = payLaterPaymentOption.getSuboptionList();
			
		}
	}
	
	public List<UIPayopSuboptionMapping> getSuboptionList() {
		return suboptionList;
	}

	public void setSuboptionList(List<UIPayopSuboptionMapping> suboptionList) {
		this.suboptionList = suboptionList;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
