package com.yatra.payment.ui.service.impl;

import java.util.HashMap;

import com.yatra.payment.ui.util.MiniServiceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.platform.commons.utils.Timeit;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

import static com.yatra.payment.ui.util.MiniServiceUtil.errorJson;

@Service
public class PassThroughServiceImpl implements MiniServiceI {
	Logger logger = Logger.getLogger(PassThroughServiceImpl.class);

	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		try{
			throw new UnsupportedOperationException("passthrough service request is no longer supported.");
//			logger.debug("Getting required Data for Pass through Node .");
//			logger.debug("Mandatory Parameter for this service is pricing ID , ssoToken.");
//			Timeit.timeIt();
//			JSONObject resultJSON = validateMandatoryParameters(requestJson);
//			if(resultJSON.length() >0){
//				return resultJSON;
//			}
//			String url = YatraUtil.getPropertyManager().getProperty("passthrough.endpoint");
//			HttpEndPoint endPoint = new HttpEndPoint(url,"GET");
//			String princingId = requestJson.getString("pricingId");
//			HashMap<String,String> parameters = new HashMap();
//			logger.debug("Hitting B2B service with following pricing ID " + princingId );
//			parameters.put("pricingId", princingId);
//			parameters.put("ssoToken", requestJson.getString("ssoToken"));
//			HttpService service = new HttpService();
//			String response = service.invoke(endPoint , parameters);
//			logger.debug("Response received from B2B passthrough service " + response );
//			Timeit.timeUp();
//			logger.debug("Time taken by Booking Amount Service "+Timeit.timeTaken());
//			return new JSONObject(response);
		}
		catch(Exception ex){
			logger.error("An error occurred",ex);
			return errorJson(ex);
		}
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		logger.debug("Validating parameters ");
		JSONObject validationJson = new JSONObject();
		try {
			if(!requestJson.has("pricingId")){
				validationJson.accumulate("error", "pricingId not found .");
				return validationJson;
			}
			String pricingId = requestJson.getString("pricingId");
			if(StringUtils.isBlank(pricingId)){
				validationJson.accumulate("error", "pricingId is blank .");
			}

			if(!requestJson.has("ssoToken")){
				validationJson.accumulate("error", "ssoToken not found .");
				return validationJson;
			}
			String ssoToken = requestJson.getString("ssoToken");
			if(StringUtils.isBlank(ssoToken)){
				validationJson.accumulate("error", "ssoTOken is blank .");
			}
		
		} catch (JSONException e) {
			logger.error(e);
		}
		return validationJson;
	}

	@Override
	public String getResultKey() {
		return "passthrough";
	}

}
