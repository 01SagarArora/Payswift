package com.yatra.payment.ui.payswift.service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.RequestResponseEnhancementHelper;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

@Service
public class PaymentProcessorImpl implements PaymentProcessor {

	@Autowired private HttpUtil httpUtil;
	@Autowired private RequestResponseEnhancementHelper rreHelper;
	
	public static final String BANK_CODE_UPI = "UPI";
	public static String STATUS_TRUE = "true";
	
	
	
	Logger logger = Logger.getLogger(PaymentProcessorImpl.class);

	@Override
	public PayNowResponse processPayment(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {

		try {
			logger.debug("Entering processPayment for :" + payNowRequest.getSuperPnr());
			
			// Hit Payment Portal to initiate payment.
			String paymentResponse = doInitPayment(payNowRequest, payNowResponse);
			JSONObject responseJSON = new JSONObject(paymentResponse);

			if (!responseJSON.getBoolean("suc")) {
				setGenericMesage(payNowResponse);
			}

			// return payment portal response in paynow response
			Map<String, String> map = convertJsontoMap(responseJSON);
			payNowResponse.setRurl(null);
			payNowResponse.setAjax(false);
			payNowResponse.setFailRedirectMap(null);
			if(!payNowRequest.isNonSDKFlow()) {
				payNowResponse.setPollStatusUrl(null);
				payNowResponse.putInRedirectMap(map);
			} else {
				payNowResponse.setInitPaymentMap(map);
			}
			return payNowResponse;
		} catch(PayswiftException pex) {
			logger.debug("An Exception Occured while hitting Payment Portal for processPayment ", pex);
			setGenericMesage(payNowResponse);
			payNowResponse.setAjax(false);
			payNowResponse.setFailRedirectMap(rreHelper.getFailRedirectData(payNowResponse));
		} catch (Exception ex) {
			logger.debug("An Exception Occured while hitting Payment Portal for processPayment ", ex);
		}
		return payNowResponse;
	}

	private void setGenericMesage(PayNowResponse payNowResponse) {
		payNowResponse.setRurl(null);
		payNowResponse.setSuccess(false);
		payNowResponse.setRedirect(false);
		payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
	}

	private String doInitPayment(PayNowRequest payNowRequest, PayNowResponse payNowResponse)
			throws UnsupportedEncodingException, MalformedURLException {
		logger.debug("Inside doInitPayment for superPnr :" + payNowRequest.getSuperPnr());

		String responseJson;
		String superPnr = payNowRequest.getSuperPnr();
		String url = createUrl(payNowResponse);
		
		// Create Request map
		Map<String, String> requestMap = createRequestMap(payNowRequest, payNowResponse);//requestMap.put("product_code","mdomandroid")
		String postData = httpUtil.createPostDataFromMap(requestMap);
		logger.info("Going to hit PP with postData: "+postData +"for pnr:"+ superPnr);

		// Post request on Payment Portal URL.
		try {
		responseJson = httpUtil.invoke(url, postData,"DO_INIT_PAYMENT");
		logger.info("Response from Payment Portal for SuperPnr: "+superPnr + " : " + responseJson);
		}
		catch(Exception e) {
			throw new PayswiftException(FailureCode.DO_INIT_PAYMENT_EXCEPTION.getCode(), "Exception while making doInitPayment Call", e);
		}
		return responseJson;
	}

	private String createUrl(PayNowResponse payNowResponse) throws MalformedURLException {
		String url = payNowResponse.getRurl();
		String host ="payment.service";
		
		URL ur = new URL(url);
		url = PaymentUIUtil.HTTP_PROTOCOL_URL_PREFIX + host + ur.getPath();
		return url;
	}

	 public Map<String, String> convertJsontoMap(JSONObject object) throws JSONException {
			Map<String, String> map = new HashMap<String, String>();

			Iterator<String> keysItr = object.keys();
			while(keysItr.hasNext()) {
				String key = keysItr.next();
				Object value;
				try {
					value = object.get(key);


					if(value instanceof JSONObject) {
						value = convertJsontoMap((JSONObject) value);
					}

					map.put(key, value.toString());
				} catch (org.json.JSONException e) {
					logger.error(e.getStackTrace());
				}
			}
			return map;
		}
	private Map<String, String> createRequestMap(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.putAll(payNowResponse.getRedirectMap());
		requestMap.put("Package", payNowRequest.getPackageName());
		requestMap.put("SDKVersion", payNowRequest.getSdkVersion());
		String vpa = payNowRequest.getParameter("payerVA");
		if(YatraUtil.isNullOrEmpty(vpa))
			vpa = payNowRequest.getParameter("gpayerVA");
		requestMap.put("payerVA", vpa);
		requestMap.put("initPayment", "true");
		requestMap.put(PaymentUIUtil.IS_NON_SDK_FLOW, String.valueOf(payNowRequest.isNonSDKFlow()));
		return requestMap;
	}

}
