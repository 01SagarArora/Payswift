package com.yatra.payment.ui.payswift.service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;

public interface PayNowProcessor {
	
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse);

}
