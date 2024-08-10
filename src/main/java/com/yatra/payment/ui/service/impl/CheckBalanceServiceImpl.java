package com.yatra.payment.ui.service.impl;

import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.AgentProfileService;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.service.MiniServiceI;

import static com.yatra.payment.ui.enums.FailureCode.FETCH_CREDITPOOL_BALANCE_EXCEPTION;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_CREDITPOOL_BALANCE_INVALID_RESPONSE_CODE;
import static com.yatra.payment.ui.util.MiniServiceUtil.errorJson;

import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CheckBalanceServiceImpl implements MiniServiceI {

	Logger logger = Logger.getLogger(CheckBalanceServiceImpl.class);
    
    @Autowired private AgentProfileService agentProfileService;
    
	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		logger.debug("Getting required Data for Credit Pool Balance Node.");
		logger.debug("Mandatory Parameter for this service is ssoToken .");
		JSONObject resultJSON = validateMandatoryParameters(requestJson);
		if(resultJSON != null){
			return resultJSON;
		}
        
		try{
            String ssoToken = requestJson.getString("ssoToken");
            if (agentProfileService.isCrpProduct(requestJson.getString("product"))) {
                net.sf.json.JSONObject userDetails = agentProfileService.getUserDetailsCorp(ssoToken);
                resultJSON = new JSONObject(agentProfileService.getCreditPoolCorp(userDetails,ssoToken,requestJson.optString("tripId", "")).toString());
                return resultJSON;
            }
			String url = YatraUtil.getPropertyManager().getProperty("agent.profile.service.endpoint");
			HttpEndPoint endPoint = new HttpEndPoint(url,"POST");
			
			logger.debug("Hitting B2B service for cash/credit pool balance with sso token  " + ssoToken );
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			HttpService service = new HttpService();
			String postData = generateRequestJSON(ssoToken);
			logger.debug("Post data generated is "+postData);
			String response = service.invoke(endPoint ,null , headers , postData ,null,null,true);
			logger.debug("Response received from B2B cash/credit pool balance service " + response );
			resultJSON = new JSONObject(response);
		}
		catch(Exception ex){
			logger.error(ex);
		}
		return resultJSON;
	}

	private String generateRequestJSON(String ssoToken) {
		JSONObject reqJson = new JSONObject();
		try {
			reqJson.accumulate("method", "checkBalance");
			reqJson.accumulate("ssoToken", ssoToken);
			reqJson.accumulate("application", "web");
			
		} catch (JSONException e) {
			logger.debug("Exception occurred in creating JSON for request ",e);
		}
		return reqJson.toString();
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		try{
			logger.debug("Validating input for check balance API ");
			logger.debug("Mandatory paramter is ssoToken ");
			JSONObject validationJSON = new JSONObject();
			if(!requestJson.has("ssoToken")){
				validationJSON.accumulate("error", "ssoToken not found .");
				return validationJSON;
			}
			if(StringUtils.isBlank(requestJson.getString("ssoToken"))){
				validationJSON.accumulate("error", "ssoToken is Blank .");
			}
		}catch(Exception ex){
			logger.debug(ex);
		}
		return null;
	}

	@Override
	public String getResultKey() {
		// TODO Auto-generated method stub
		return "balance";
	}

}
