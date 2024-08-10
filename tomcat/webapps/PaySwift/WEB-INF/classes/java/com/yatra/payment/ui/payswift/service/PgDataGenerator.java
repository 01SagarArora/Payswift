package com.yatra.payment.ui.payswift.service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;

public interface PgDataGenerator {
	public PayNowResponse getPGData(PayNowRequest payNowRequest) throws Exception;

}
