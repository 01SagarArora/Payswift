package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;

public class PaytmPaymentOption extends PaymentOption {
	private List<UIPayopSuboptionMapping> suboptionList;
        private String action;
        
	public PaytmPaymentOption() {}
	
	public PaytmPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof PaytmPaymentOption) {
			PaytmPaymentOption paytmPaymentOption = (PaytmPaymentOption) paymentOption;
			this.suboptionList = paytmPaymentOption.getSuboptionList();
			this.action = "paynow";
		}
		
	}

        public String getAction() {
            return action;
        }

	public List<UIPayopSuboptionMapping> getSuboptionList() {
		return suboptionList;
	}

	public void setSuboptionList(List<UIPayopSuboptionMapping> suboptionList) {
		for(UIPayopSuboptionMapping subOption : suboptionList) {
			subOption.setPaymentOption("mw");
		}
		this.suboptionList = suboptionList;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}

