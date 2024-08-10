package com.yatra.payment.ui.payswift.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;

@Service
public class CredPaymentService {

	private static Logger logger = Logger.getLogger(PaytmPaymentService.class);
	private static final Map<String, String> EMPTY_MAP = new HashMap<>();

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
			logger.error("Error inside Cred Payment service, superPnr "+ payNowRequest.getSuperPnr(),e);
			setGenericMesage(payNowResponse);
			ppResponse = e.getMessage();
		}
		//payment portal response
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_RES, ppResponse, payNowRequest.getProductCode());
		// response returned to UI
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_NOW_RES_FINAL, payNowResponse.toString(), payNowRequest.getProductCode());

		return payNowResponse;
	}
	private String sendRequestToPaymentPortal(PayNowRequest payNowRequest, PayNowResponse payNowResponse) throws Exception{
		String superPnr = payNowRequest.getSuperPnr();
		logger.debug("Inside sendRequestToPaymentPortal() for superPnr :" + superPnr);
		Map<String, String> requestMap = payNowResponse.getRedirectMap();
		String url = propertyManager.getProperty("cred.payment.portal.url");

		logger.info("Inside sendRequestToPaymentPortal() for Cred App flow");
		requestMap.put(PaymentUIUtil.BANK_CODE, PaymentUIUtil.CRED_BANK_CODE);

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

	private Map<String, String> getRedirectMap(JSONObject ppJSON) throws JSONException, JsonParseException, JsonMappingException, IOException{

		Iterator<String> keys = ppJSON.keys();
		Map<String, String> map = new HashMap<String,String>();
		while(keys.hasNext()) {
			String key = keys.next();
			if (ppJSON.get(key) instanceof  Integer) {
				Integer value = ppJSON.getInt(key);
				map.put(key, Integer.toString(value));
			}
			else if(ppJSON.get(key) instanceof  Boolean) {
				Boolean value = ppJSON.getBoolean(key);
				map.put(key, Boolean.toString(value));
			}
			else {
				String value = ppJSON.getString(key);
				map.put(key, value);
			}
		}
		return map;
	}
}