package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;

public class PhonePePaymentOption extends PaymentOption {
	private List<UIPayopSuboptionMapping> suboptionList;
        private String action;
        
	public PhonePePaymentOption() {}
	
	public PhonePePaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof PhonePePaymentOption) {
			PhonePePaymentOption phonePePaymentOption = (PhonePePaymentOption) paymentOption;
			this.suboptionList = phonePePaymentOption.getSuboptionList();
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
			subOption.setPaymentOption("phonePe");
		}
		this.suboptionList = suboptionList;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}









