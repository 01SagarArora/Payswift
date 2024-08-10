package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.QuickBookSaveCardService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;

@Service
public class PassThroughHandler implements PayNowProcessor {

	@Autowired
	QuickBookSaveCardService quickBookSaveCardService;
	@Autowired
	private PayNowProcessor b2BImportPNRFlowHandler;

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		Logger logger = Logger.getLogger(PassThroughHandler.class);
			boolean isPassThrough = payNowRequest.getBookingDetails().isPassThrough();
			if (isPassThrough && (payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD) || 
					payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD)) ) {
				// Make a separate request to Save User Card and fetch Card ID .
				
				logger.debug("Entering passthrough flow ");
				String ssoToken = payNowRequest.getSSOToken();
				Map<String, String> requestMap = createRequestMap(payNowRequest);
				String cardId = quickBookSaveCardService.saveQBCardTemporarily(ssoToken, requestMap);
				logger.info("cardId recieved in response of QB saveQBCardTemporarily : "+cardId + " super_pnr "+ payNowRequest.getSuperPnr());

				/* checking failure of saveCardTemporarily for crpdomhotel passthrough */
				if ((payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD)
						|| payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD))
						&& StringUtils.equals(payNowRequest.getProductCode(),"crpdomhotel")
						&& (StringUtils.isBlank(cardId) || StringUtils.equalsIgnoreCase(cardId,"null"))  ) {
					logger.debug("RequestResponseEnhancer : passthrough flow but card did not get saved successfully for superPnr,product : "+payNowRequest.getSuperPnr()+","+payNowRequest.getProductCode());
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.PASSTHROUGH_FLOW_SAVE_CARD_TEMPORARILY_FAILURE);
					return  payNowResponse;
				}
				payNowRequest.setCardId(cardId);
			}
		
			return b2BImportPNRFlowHandler.process(payNowRequest, payNowResponse);
		
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
