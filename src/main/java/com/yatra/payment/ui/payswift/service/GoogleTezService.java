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
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.util.YatraUtil;

@Service
public class GoogleTezService {

	private static Logger logger = Logger.getLogger(GoogleTezService.class);
	private static final Map<String, String> EMPTY_MAP = new HashMap<>();
	public static final String ORDER_TOTAL_CURRENCY_CODE = "orderTotalCurrencyCode";

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
				payNowResponse.putInPgData(getRedirectMap(ppJSON, payNowRequest, payNowResponse));
			}
		}catch(Exception e){
			logger.error("Error inside Google Tez service, superPnr "+ payNowRequest.getSuperPnr(),e);
			setGenericMesage(payNowResponse);
			ppResponse = e.getMessage();
		}
		//payment portal response
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_RES, ppResponse, payNowRequest.getProductCode());
		// response returned to UI
		loggingProcessor.logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_NOW_RES_FINAL_GOOGLE_TEZ, payNowResponse.toString(), payNowRequest.getProductCode());

		return payNowResponse;
	}

	private String sendRequestToPaymentPortal(PayNowRequest payNowRequest, PayNowResponse payNowResponse) throws Exception{
		String superPnr = payNowRequest.getSuperPnr();
		logger.debug("Inside sendRequestToPaymentPortal() for superPnr :" + superPnr);
		Map<String, String> requestMap = payNowResponse.getRedirectMap();
		String url = propertyManager.getProperty("google.tez.payment.portal.url");
		if(payNowRequest.isGoogleTezFlow()) {
			logger.info("Inside sendRequestToPaymentPortal() for PWA googleTezFlow");
			requestMap.put(PaymentUIUtil.BANK_CODE, PaymentUIUtil.GOOGLE_TEZ_BANK_CODE);
		}
		
		else if(payNowRequest.isGoogleTezAndroidFlow()) {
			logger.info("Inside sendRequestToPaymentPortal() for Android googlePayAndroidFlow");
			requestMap.put(PaymentUIUtil.BANK_CODE, PaymentUIUtil.GOOGLE_ANDROID_BANK_CODE);
		}
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

	private Map<String, String> getRedirectMap(JSONObject ppJSON, PayNowRequest payNowRequest, PayNowResponse payNowResponse) throws JSONException{
		Map<String, String> redirectMap = new HashMap<>();
		String product = payNowRequest.getBookingDetails().getProductCode();
		if("gpaydom".equalsIgnoreCase(product) || "gpayint".equalsIgnoreCase(product) || "gpadom_hotel".equalsIgnoreCase(product)) {
			redirectMap.put("gpay_spot", "true");
			//adding promo specific handling
			if(payNowResponse.getPromoSuccess() && !YatraUtil.isNullOrEmpty(payNowResponse.getPromoAmount())) {
				redirectMap.put("gpay_promo_code", payNowRequest.getBookingDetails().getPromoCode());
				redirectMap.put("gpay_promo_amount", payNowResponse.getPromoAmount());
			}
		} else redirectMap.put("gpay_spot", "false");
		
		redirectMap.put(PaymentUIUtil.MERCHANT_TXN_ID, ppJSON.getString(PaymentUIUtil.MERCHANT_TXN_ID));
		redirectMap.put(PaymentUIUtil.AMOUNT, ppJSON.getString(PaymentUIUtil.AMOUNT));
		populateGateway(redirectMap, ppJSON);
        if("gpay_android".equalsIgnoreCase(ppJSON.getString("pg")))
        	redirectMap.put("rurl", ppJSON.getString(PaymentUIUtil.RURL));
        else redirectMap.put("responseUrl", ppJSON.getString(PaymentUIUtil.RURL));
        if(ppJSON.has(PaymentUIUtil.PAY_BY_DURTN_SEC)) {
        	redirectMap.put(PaymentUIUtil.PAY_BY_DURTN_SEC,ppJSON.getString(PaymentUIUtil.PAY_BY_DURTN_SEC));
        }
        if(ppJSON.has(PaymentUIUtil.POLL_INTERVAL_SEC)) {
        	redirectMap.put(PaymentUIUtil.POLL_INTERVAL_SEC,ppJSON.getString(PaymentUIUtil.POLL_INTERVAL_SEC));
        }
        if(ppJSON.has(PaymentUIUtil.POLL_DURATION_AFTER_GETTING_APP_CONTROL)) {
        	redirectMap.put(PaymentUIUtil.POLL_DURATION_AFTER_GETTING_APP_CONTROL,ppJSON.getString(PaymentUIUtil.POLL_DURATION_AFTER_GETTING_APP_CONTROL));
        }
        if(ppJSON.has("pa"))
        	redirectMap.put("pa", ppJSON.optString("pa"));
        if(ppJSON.has("mcc"))
        	redirectMap.put("mcc", ppJSON.optString("mcc"));
        if(ppJSON.has("isCardSupported") && payNowRequest.getProductCode().contains("android")&& payNowRequest.getAppVersion()!=null && 366<=Integer.valueOf(payNowRequest.getAppVersion())) {
        	redirectMap.put("isCardSupported",ppJSON.getString("isCardSupported"));
        	redirectMap.put("allowedCardNetworks",ppJSON.getString("allowedCardNetworks"));
        	redirectMap.put("cardReturnUrl",ppJSON.getString("cardReturnUrl"));
        	redirectMap.put("gateway", ppJSON.getString("gateway"));
        	redirectMap.put("gatewayMerchantId", ppJSON.getString("gatewayMerchantId"));
        	redirectMap.put("gatewayTransactionId", ppJSON.getString("gatewayTransactionId"));
        }
        
		return redirectMap;
	}
	
	private void populateGateway(Map<String, String> redirectMap, JSONObject ppJSON) throws JSONException {
		String gateway = ppJSON.getString("pg");
		String identifyingGateway = null;
		if("gpay_android_rzr".equalsIgnoreCase(gateway) || "gpay_android".equalsIgnoreCase(gateway) || "gpay_android_pyu".equalsIgnoreCase(gateway) || "gpay_android_hdf".equalsIgnoreCase(gateway)) {
			identifyingGateway = "gpay_android";
			redirectMap.put("yatra_orderId", ppJSON.getString("yatra_orderId"));
		}
		if("google_tez_rzr".equalsIgnoreCase(gateway) || "google_tez".equalsIgnoreCase(gateway) || "google_tez_payu".equalsIgnoreCase(gateway) || "google_tez_hdf".equalsIgnoreCase(gateway))
			identifyingGateway = "google_tez";
		 redirectMap.put("pg",identifyingGateway);
		 redirectMap.put("processingGateway",gateway);
		
	}

}
