package com.yatra.payment.ui.service.impl;

import com.yatra.payment.ui.util.MiniServiceUtil;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.platform.util.YatraUtil;

import static com.yatra.payment.ui.util.MiniServiceUtil.errorJson;

@Service
public class SurchargeServiceImpl implements MiniServiceI{

	@Autowired private AgentProfileService agentProfileService;
	Logger logger = Logger.getLogger(SurchargeServiceImpl.class);
	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		String superPnr="";
		try{
			throw new UnsupportedOperationException("surcharge service request is no longer supported.");
//			superPnr = requestJson.getString("superPnr");
//			JSONObject resultJson = new JSONObject();
//			logger.debug("adding surcharge node begins for PNR  "+ superPnr);
//			net.sf.json.JSONObject agentPaymentOptionJSON = (net.sf.json.JSONObject) agentProfileService.getAgentPaymentOptionsAsJsonObject(requestJson.getString("ssoToken"),requestJson.getString("product"));
//			net.sf.json.JSONObject surchargeJSON = agentPaymentOptionJSON.getJSONObject("surcharge");
//			logger.debug("surcharge JSON found " + surchargeJSON);
//			resultJson.accumulate("surcharge", surchargeJSON);
//			return resultJson;
		}
		catch(Exception ex){
			logger.debug("Exception occurred for fetching surcharge for pnr "+superPnr,ex);
			return errorJson(ex);
		}
	}
		

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		JSONObject errorJson = new JSONObject();
	    StringBuffer bufferedError = new StringBuffer();
	    if(!requestJson.has("product")) 
	        bufferedError.append("product not present");
	    if(!requestJson.has("ssoToken")) 
	        bufferedError.append("ssoToken not present");
	    
	    String error = null;
	    error = bufferedError.toString();
	    
	    if(!YatraUtil.isNullOrEmpty(error)) {
	        try {
	            return errorJson.put("error", error);
	        } catch (JSONException e) {
	            logger.info("Error while preparing Quickbook Error Json", e);
	        }
	    }
	    
	    return null;
	   
}

	@Override
	public String getResultKey() {
		// TODO Auto-generated method stub
		return "surcharge";
	}

}
