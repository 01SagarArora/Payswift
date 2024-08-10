package com.yatra.payment.ui.service.impl;

import static com.yatra.payment.ui.util.MiniServiceUtil.PAYMENT_INFO_API;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.util.MiniServiceUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;


@Service
public class CorporateBinsConfigServiceImpl implements MiniServiceI {

	private static final Logger logger = Logger.getLogger(CorporateConfigurationServiceImpl.class);
	private static final String CORP_CONFIG_SERVICE_URL = "corporate.config.service.endpoint";
	private static final JSONArray CACHED_BIN_CONFIG_ARR;

	@Autowired PropertyManager properties;
	@Autowired HttpService httpService;
	@Autowired PaymentUIHelper paymentUIHelper;

	static {
		CACHED_BIN_CONFIG_ARR = buildBinConfigsArrForPayopApi();
	}

	@Override	
	public  JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		String superPnr = null;
		JSONObject errorBinList = validateMandatoryParameters(requestJson);
		if(errorBinList!= null) {
			paymentUIHelper.logIntoDB(errorBinList.toString(),"RESPONSE", superPnr, "SkipOTPBinList");
			return errorBinList;
		} else {
			try {
				superPnr = requestJson.getString("superPnr");
				String ssoToken = requestJson.getString("ssoToken");
				String bookingType = requestJson.getString("bookingType");
				String configProduct = requestJson.getString("productForFalcon");
				String url = properties.getProperty(CORP_CONFIG_SERVICE_URL);
				String postData = buildReqJson(ssoToken);
				logger.debug("Hitting Corporate Bin Configuration Service with params :" + postData );
				HttpEndPoint serviceEndPoint = new HttpEndPoint(url,"POST");
				String response = httpService.invoke(serviceEndPoint, null, null, postData);
				logger.info("Received response from Corporate Bin Configuration Service: " + response);
				JSONObject respJson = new JSONObject(response);
				
				if(!respJson.has("success") || !respJson.getBoolean("success")) {
					paymentUIHelper.logIntoDB(getErrorBinList().toString(),"RESPONSE", superPnr, "SkipOTPBinList");
				}
				else {
					JSONObject binJson = respJson.getJSONObject("configurations")
							.getJSONObject("binsConfig")
							.getJSONObject(configProduct)
							.getJSONObject(bookingType.toLowerCase());
					if(binJson.has("binsForSkipOtp")) {
						paymentUIHelper.logIntoDB(binJson.toString(),"RESPONSE", superPnr, "SkipOTPBinList");
						return binJson;
					}
					else {
						paymentUIHelper.logIntoDB(getErrorBinList().toString(),"RESPONSE", superPnr, "SkipOTPBinList");
					}
				}
			} catch (Exception ex){
				logger.error(PAYMENT_INFO_API + ":" + "FETCH_CORP_BIN_CONFIG_SERVICE_EXCEPTION" + " for superPnr : " + superPnr, ex);
				paymentUIHelper.logIntoDB(getErrorBinList().toString(),"RESPONSE", superPnr, "SkipOTPBinList");
			}
		}
		return errorBinList;

	}

	private JSONObject getErrorBinList() {
		JSONObject errorJson = new JSONObject();
		try {
			ArrayList<String> binList = new ArrayList<String>();
			return errorJson.put("binsForSkipOtp", binList);
		} catch (JSONException e) {
			logger.info("Error while preparing CorporateConfigurationService Error Json", e);
		}
		return null;
	}

	private String buildReqJson(String ssoToken) throws Exception{

		JSONObject reqJson = new JSONObject();
		reqJson.put("ssoToken", ssoToken);
		reqJson.put("configs", CACHED_BIN_CONFIG_ARR);

		return reqJson.toString();
	}

	private static JSONArray buildBinConfigsArrForPayopApi() {

		JSONArray configArr = new JSONArray();
		try {
			JSONObject whereConditionArrElem1 = new JSONObject();
			whereConditionArrElem1.put("name", "channel");
			whereConditionArrElem1.put("value", "web");

			JSONArray whereConditionArr = new JSONArray();
			whereConditionArr.put(whereConditionArrElem1);

			JSONObject configArrElem1 = new JSONObject();
			configArrElem1.put("name", "binsConfig");
			configArrElem1.put("whereConditions", whereConditionArr);

			configArr.put(configArrElem1);
		} catch (Exception e) { 
			logger.error("Error while creating Request for CorporateBinConfigurationService", e);
		}

		return configArr;
	}



	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		try {
			if(!requestJson.has("ssoToken")) {
				logger.error("ssoToken is not present in the request, returning error.");
				return getErrorBinList();
			}
		} catch (Exception e) {
			logger.error("Exception while getting ssoToken from request, hence returning blank list");
			return getErrorBinList();
		}
		return null;
	}

	@Override
	public String getResultKey() {
		return MiniServiceUtil.CORP_BIN_CONFIG_SERVICE_KEY;
	}
}

