package com.yatra.payment.ui.payswift.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;

@Service
public class CommonPgDataGenerator implements PgDataGenerator {
	
	@Autowired
	private PayNowProcessor loggingProcessor;

	@Override
	public PayNowResponse getPGData(PayNowRequest payNowRequest) throws Exception {
		PayNowResponse payNowResponse = new PayNowResponse();
		//return preProcessingProcessor.process(payNowRequest, payNowResponse);
		return loggingProcessor.process(payNowRequest, payNowResponse);
	}

}
