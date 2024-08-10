package com.yatra.payment.ui.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.base.log.Logger;
import com.yatra.base.log.LoggerFactory;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

@Service
public class ECashJsonServiceImpl implements MiniServiceI {
	
	@Autowired private PropertyManager property;
	@Autowired private HttpService httpService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	@Override
	public JSONObject getRequiredData(JSONObject requestJson,
			JSONObject responseJSON) {
			// https://secure.yatra.com/checkout/wallet/get-ecash.htm?cust_email=yatratestbookings%40gmail.com&merchant_code=yatra&product_code=bus 
		 	String ssoToken = null;
			String userEmail=null;
			String product = null;
			String superPnr = null;
			JSONObject resultJSON = validateMandatoryParameters(requestJson);
			try{
				ssoToken = requestJson.getString("ssoToken");
				product = requestJson.getString("product");
				superPnr = requestJson.getString("superPnr");
				String merchant = requestJson.getString("merchant");
				
				String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/session/loginInfo";
				JSONObject userLoginInfoRequest = new JSONObject();
				userLoginInfoRequest.put("ssoToken", ssoToken);
				String userLoginInfoResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginInfoRequest.toString(), url, "1011");
				JSONObject responseJsonObject = new JSONObject(userLoginInfoResponse);
				userEmail = responseJsonObject.optString("emailId");
				
				Map<String,String> parameterMap = new HashMap<String,String>();
				parameterMap.put("ssoToken",ssoToken);
				parameterMap.put("cust_email",userEmail);
				parameterMap.put("merchant_code", merchant);
				parameterMap.put("product_code", product);
				String getEcashURL = property.getProperty("secure.get.ecash.url");
				logger.debug("fetchECashDetail Request with customerEmail {} , product_code {}, merchantCode {}",userEmail,product,merchant);
				HttpEndPoint endPoint = new HttpEndPoint(getEcashURL, "POST");
				String response = httpService.invoke(endPoint, parameterMap);
				logger.debug("Response for fetchECashDetail is {}", response);
				resultJSON = new JSONObject(response);
			}catch(Exception e){
				logger.error("Caught an Exception at fetchECashDetail on superPnr {} due to {}", superPnr, e);
			}
			return resultJSON;
		
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		JSONObject errorJson = new JSONObject();
		StringBuffer bufferedError = new StringBuffer();
		if (!requestJson.has("merchant"))
			bufferedError.append("merchant not present");
		if (!requestJson.has("product"))
			bufferedError.append("product not present");
		if (!requestJson.has("ssoToken"))
			bufferedError.append("ssoToken not present");

		String error = null;
		error = bufferedError.toString();

		if (!YatraUtil.isNullOrEmpty(error)) {
			try {
				return errorJson.put("error", error);
			} catch (JSONException e) {
				logger.info("Error while preparing ECash Error Json", e);
			}
		}

		return null;
	}

	@Override
	public String getResultKey() {
		// TODO Auto-generated method stub
		return "eCashJSON";
	}

}
