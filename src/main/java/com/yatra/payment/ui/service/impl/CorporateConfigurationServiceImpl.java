package com.yatra.payment.ui.service.impl;

import static com.yatra.payment.ui.util.MiniServiceUtil.PAYMENT_INFO_API;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.util.MiniServiceUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

@Service
public class CorporateConfigurationServiceImpl implements MiniServiceI {

	private static final Logger logger = Logger.getLogger(CorporateConfigurationServiceImpl.class);
	private static final String CORP_CONFIG_SERVICE_URL = "corporate.config.service.endpoint";
	private static final JSONArray CACHED_CONFIG_ARR;
	
	@Autowired PropertyManager properties;
	@Autowired HttpService httpService;
	
	static {
		CACHED_CONFIG_ARR = buildConfigsArrForPayopApi();
	}

	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		String superPnr = null;
		JSONObject errorJson = validateMandatoryParameters(requestJson);
		if(errorJson!= null) return errorJson;
		try {
			superPnr = requestJson.getString("superPnr");
			String ssoToken = requestJson.getString("ssoToken");
			String url = properties.getProperty(CORP_CONFIG_SERVICE_URL);
			String postData = buildReqJson(ssoToken);
			logger.debug("Hitting Corporate Configuration Service with params :" + postData );
		    HttpEndPoint serviceEndPoint = new HttpEndPoint(url,"POST");
		    String response = httpService.invoke(serviceEndPoint, null, null, postData);
		    logger.info("Received response from Corporate Configuration Service: " + response);

		    return new JSONObject(response);
		} catch (Exception ex){
			logger.error(PAYMENT_INFO_API + ":" + "FETCH_CORP_CONFIG_SERVICE_EXCEPTION" + " for superPnr : " + superPnr, ex);
			return createErrorJson("Something went wrong.");
		}
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		if(!requestJson.has("ssoToken")) {
			logger.error("ssoToken is not present in the request, returning error.");
			return createErrorJson("ssoToken not present.");
		}
		return null;
	}

	@Override
	public String getResultKey() {
		return MiniServiceUtil.CORP_CONFIG_SERVICE_KEY;
	}
	
	private JSONObject createErrorJson(String errorMessage) {
		JSONObject errorJson = new JSONObject();
			try {
				errorJson.put("success", false);
				errorJson.put("error", errorMessage);
				return errorJson;
			} catch (JSONException e) {
				logger.info("Error while preparing CorporateConfigurationService Error Json", e);
			}
		return null;
	}
	
	private String buildReqJson(String ssoToken) throws Exception{
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("ssoToken", ssoToken);
		reqJson.put("configs", CACHED_CONFIG_ARR);
		
		return reqJson.toString();
	}

	private static JSONArray buildConfigsArrForPayopApi() {

		JSONArray configArr = new JSONArray();
		try {
			JSONObject whereConditionArrElem1 = new JSONObject();
			whereConditionArrElem1.put("name", "channel");
			whereConditionArrElem1.put("value", "web");

			JSONArray whereConditionArr = new JSONArray();
			whereConditionArr.put(whereConditionArrElem1);

			JSONObject configArrElem1 = new JSONObject();
			configArrElem1.put("name", "cardTypeAccessConfig");
			configArrElem1.put("whereConditions", whereConditionArr);

			configArr.put(configArrElem1);
		} catch (Exception e) { 
			logger.error("Error while creating Request for CorporateConfigurationService", e);
		}

		return configArr;
	}
}
