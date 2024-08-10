package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.util.ExceptionUtils;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;

@Service
public class AmazonPayMobileSDKService {
	
	private static Logger logger = Logger.getLogger(AmazonPayMobileSDKService.class);
	
	private static final String AMAZON_PAY_BANK_CODE = "AMPY_SDK";
	private static final String SIGNATURE_URL_KEY = "signatureUrl";
	private static final String VERIFICATION_URL_KEY = "verificationUrl";
	private static final String REDIRECT_TO_LOB_URL_KEY = "redirectToLobUrl";
	private static final Map<String, String> EMPTY_MAP = new HashMap<>();
	public static final String ORDER_TOTAL_CURRENCY_CODE = "orderTotalCurrencyCode";
	public static final String POLL_TIMEOUT_SEC = "pollTimeoutInSec";
	public static final String POLL_INTERVAL_SEC = "pollIntervalInSec";
	
	@Autowired private LoggingProcessor loggingProcessor;
	@Autowired private HttpUtil httpUtil;
	@Autowired private PropertyManager propertyManager;
	
	public PayNowResponse processPayment(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		String ppResponse = "";
		try{
			ppResponse = sendRequestToPaymentPortal(payNowRequest, payNowResponse);
			JSONObject ppJSON = new JSONObject(ppResponse);
			if (!ppJSON.getBoolean(PaymentUIUtil.IS_SUCCESSFUL)) {
				setGenericMesage(payNowResponse);
			}else{
				payNowResponse.putInPgData(getRedirectMap(ppJSON));
			}
		}catch(Exception e){
			logger.error("Error inside amazonpay sdk service, superPnr "+ payNowRequest.getSuperPnr(),e);
			setGenericMesage(payNowResponse);
			ppResponse = ExceptionUtils.toString(e);
		}
		//payment portal response
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_RES, ppResponse, payNowRequest.getProductCode());
		// response returned to app
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_NOW_RES_FINAL, payNowResponse.toString(), payNowRequest.getProductCode());
		
		return payNowResponse;
	}
	
	private String sendRequestToPaymentPortal(PayNowRequest payNowRequest, PayNowResponse payNowResponse) throws Exception{
		String superPnr = payNowRequest.getSuperPnr();
		logger.debug("Inside sendRequestToPaymentPortal() for superPnr :" + superPnr);
		
		String url = propertyManager.getProperty("amazon.pay.sdk.payment.portal.initiate.url");
		
		Map<String, String> requestMap = payNowResponse.getRedirectMap();
		
		requestMap.put(PaymentUIUtil.BANK_CODE, AMAZON_PAY_BANK_CODE);
		
		String postData = httpUtil.createPostDataFromMap(requestMap);
		
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(),PaymentUIUtil.PAY_REQ, url+"?"+postData,payNowRequest.getProductCode());
		
		logger.info("Going to hit PP with postData: "+postData +" for pnr:"+ superPnr);

		String responseJson = httpUtil.invoke(url, postData,"DO_INIT_PAYMENT");
		logger.info("Response from Payment Portal for SuperPnr: "+superPnr + " : " + responseJson);
		
		return responseJson;
	}
	
	private void setGenericMesage(PayNowResponse payNowResponse) {
		payNowResponse.setRurl(null);
		payNowResponse.setSuccess(false);
		payNowResponse.setRedirect(false);
		payNowResponse.putInRedirectMap(EMPTY_MAP);
		payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
	}
	
	private Map<String, String> getRedirectMap(JSONObject ppJSON) throws JSONException{
		
		Map<String, String> redirectMap = new HashMap<>();
		redirectMap.put(PaymentUIUtil.MERCHANT_TXN_ID, ppJSON.getString(PaymentUIUtil.MERCHANT_TXN_ID));
		redirectMap.put(SIGNATURE_URL_KEY, ppJSON.getString(SIGNATURE_URL_KEY));
		redirectMap.put(VERIFICATION_URL_KEY, ppJSON.getString(VERIFICATION_URL_KEY));
		redirectMap.put(REDIRECT_TO_LOB_URL_KEY, ppJSON.getString(REDIRECT_TO_LOB_URL_KEY));
		redirectMap.put(PaymentUIUtil.AMOUNT, ppJSON.getString(PaymentUIUtil.AMOUNT));
		redirectMap.put(ORDER_TOTAL_CURRENCY_CODE, ppJSON.getString(ORDER_TOTAL_CURRENCY_CODE));
		redirectMap.put(POLL_INTERVAL_SEC, ppJSON.getString(POLL_INTERVAL_SEC));
		redirectMap.put(POLL_TIMEOUT_SEC, ppJSON.getString(POLL_TIMEOUT_SEC));
		return redirectMap;
	
	}
	
}
