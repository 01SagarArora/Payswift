package com.yatra.payment.ui.display.beans;

public class ZestMoneyPaymentOption extends PaymentOption {
	private String action;
	
	public ZestMoneyPaymentOption() {}

	public ZestMoneyPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof ZestMoneyPaymentOption) {
			/*ZestMoneyPaymentOption zestMoneyPaymentOption = (ZestMoneyPaymentOption) paymentOption;*/
			this.action = "paynow";
		}
		
	}
	
	public String getAction() {
        return action;
    }
	

}

