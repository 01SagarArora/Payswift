package com.yatra.payment.ui.builder;

import com.yatra.payment.ui.display.beans.PaymentOption;

public interface PaymentOptionBuilder {

	public PaymentOption build(PaymentOption paymentOption) throws Exception;

	public PaymentOption build(String merchantCode, String productCode) throws Exception;
	
}
