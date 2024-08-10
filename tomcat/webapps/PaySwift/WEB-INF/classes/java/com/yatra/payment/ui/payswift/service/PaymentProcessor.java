package com.yatra.payment.ui.payswift.service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;

public interface PaymentProcessor {
	public PayNowResponse processPayment(PayNowRequest payNowRequest, PayNowResponse payNowResponse);
}
