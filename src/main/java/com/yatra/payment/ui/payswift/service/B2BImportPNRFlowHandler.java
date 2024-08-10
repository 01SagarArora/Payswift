package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.AgentQBSaveCardService;
import com.yatra.payment.ui.service.QuickBookSaveCardService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;

@Service
public class B2BImportPNRFlowHandler implements PayNowProcessor{

	@Autowired
	AgentQBSaveCardService agentQBSaveCardService;
	@Autowired
	private PayNowProcessor temporarilySaveCardWithCVVProcessor;

	Logger logger = Logger.getLogger(B2BImportPNRFlowHandler.class);
	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		boolean isImportPNRFlow = "b2bimportpnr".equalsIgnoreCase(payNowRequest.getBookingDetails().getProductCode());
		if (isImportPNRFlow && (payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD) || 
				payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD)) ) {
			// Make a separate request to Save User Card and fetch Card ID .

			logger.debug("Entering b2b import pnr flow");
			String ssoToken = payNowRequest.getSSOToken();
			try {
			Map<String, String> requestMap = createRequestMap(payNowRequest);
			String response = agentQBSaveCardService.saveB2BQBCardTemporarily(ssoToken, requestMap, payNowRequest.getCardBin());
			JSONObject resJson = new JSONObject(response);
			payNowRequest.setCardId(resJson.optString(PaymentUIUtil.CARD_ID, null));
			payNowRequest.setB2BAgentId(resJson.optString(PaymentUIUtil.AGENT_ID, null));
			} catch (Exception ex) {
				throw new PayswiftException(FailureCode.SAVE_IMPORTPNR_CARD_EXCEPTION.getCode(), "An Exception Occured while saving card for import pnr flow booking", ex);
			}
		}

		return temporarilySaveCardWithCVVProcessor.process(payNowRequest, payNowResponse);

	}

	private Map<String, String> createRequestMap(PayNowRequest payNowRequest) {
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put(PaymentUIUtil.MERCHANT_CODE, payNowRequest.getMerchantCode());
		requestMap.put(PaymentUIUtil.PRODUCT_CODE, payNowRequest.getProductCode());
		requestMap.putAll(payNowRequest.getPaymentOptionParamMap());
		requestMap.put(PaymentUIUtil.CARD_TYPE, payNowRequest.getPaymentOption());
		requestMap.put(QuickBookUIUtil.CARD_BRAND, payNowRequest.getCardType());
		requestMap.put(PaymentUIUtil.BOOKING_TYPE, "ALL");
		return requestMap;
	}

}
