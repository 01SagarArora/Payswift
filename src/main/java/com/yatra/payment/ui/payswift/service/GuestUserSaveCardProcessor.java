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
import com.yatra.payment.ui.service.QuickBookSaveCardService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.payment.ui.util.SSOAndUPSCode;
import com.yatra.platform.commons.role.CallContextKeeper;

@Service
public class GuestUserSaveCardProcessor implements PayNowProcessor {
	@Autowired
	QuickBookSaveCardService quickBookSaveCardService;
	@Autowired 
	PayNowProcessor passThroughHandler;
	
	Logger logger = Logger.getLogger(GuestUserSaveCardProcessor.class);
	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {

		boolean saveGuestCard = payNowRequest.getBookingDetails().saveGuestCard();
		if (saveGuestCard && payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK)){
			Long userId = fetchUserIdFromSsoToken();
			payNowRequest.setUserId(userId.toString());
		}
		if (saveGuestCard && 
				(payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD) ||
						payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD))) {
				// Make a separate request to Save User Card and fetch Card ID .
				logger.debug("Saving guest user card . ");
				Map<String, String> requestMap = createRequestMap(payNowRequest);
				logger.debug("Parameter map created is "+ requestMap);
				String cardId = quickBookSaveCardService.saveGuestUserCard(requestMap);
				logger.debug("Card Id received from quickbook service is "+cardId);
				payNowRequest.setCardId(cardId);
		}

		return passThroughHandler.process(payNowRequest, payNowResponse);
	}

	private Long fetchUserIdFromSsoToken() {
		String  ssoToken = CallContextKeeper.getCallContext().getSsoToken();
		try {
			String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/session/loginInfo";
			JSONObject userLoginInfoRequest = new JSONObject();
			userLoginInfoRequest.put("ssoToken", ssoToken);
			String userLoginInfoResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginInfoRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(userLoginInfoResponse);
		
			if (! SSOAndUPSCode.SUCCESSFULLY_RETRIEVED_USER_LOGIN_INFO.equals(responseJsonObject.optString("code"))) {
				throw new PayswiftException(FailureCode.INVALID_USER_ERROR_MESSAGE.getCode(), "The User needs to login again. The login is invalid or too old for this operation");
			}
			logger.debug("User id returned from user profile service " + responseJsonObject.optLong("userId"));
			return responseJsonObject.optLong("userId");
		} catch(Exception e) {
			throw new PayswiftException(FailureCode.INVALID_USER_ERROR_MESSAGE.getCode(), "Exception encountered while retreiving userId for ssoToken");
		}

	}

	private Map<String, String> createRequestMap(PayNowRequest payNowRequest) {
		Map<String,String> requestMap = new HashMap<String, String>();
		requestMap.put(PaymentUIUtil.MERCHANT_CODE, payNowRequest.getMerchantCode());
		requestMap.put(PaymentUIUtil.PRODUCT_CODE, payNowRequest.getProductCode());
		requestMap.putAll(payNowRequest.getPaymentOptionParamMap());
		requestMap.put(PaymentUIUtil.CARD_TYPE, payNowRequest.getPaymentOption());
		requestMap.put(QuickBookUIUtil.CARD_BRAND, payNowRequest.getCardType());
		requestMap.put(PaymentUIUtil.USER_ID, payNowRequest.getUserId());
		return requestMap;
	}

}
