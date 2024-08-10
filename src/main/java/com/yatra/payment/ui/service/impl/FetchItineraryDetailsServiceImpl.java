package com.yatra.payment.ui.service.impl;

import com.yatra.base.log.Logger;
import com.yatra.base.log.LoggerFactory;
import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.payswift.beans.LobDetailRequest;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.payswift.service.LobDetailsService;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.platform.util.YatraUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.yatra.payment.ui.enums.FailureCode.FETCH_ITINERARY_EXCEPTION;
import static com.yatra.payment.ui.util.MiniServiceUtil.BOOKING_DETAILS_KEY;
import static com.yatra.payment.ui.util.MiniServiceUtil.PAYMENT_INFO_API;

@Service
public class FetchItineraryDetailsServiceImpl implements MiniServiceI {

	private Logger logger = LoggerFactory.getLogger();
	
	@Autowired private LobDetailsService lobDetailsService;
	
	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		String superPnr = null;
		JSONObject errorJson = validateMandatoryParameters(requestJson);
		if(errorJson!= null) return errorJson;
		try{
			superPnr = requestJson.getString("superPnr");
			LobDetailRequest lobDetailRQ = new LobDetailRequest(superPnr, 
					requestJson.has("client") ? requestJson.getString("client") : null,
					requestJson.has("uuid") ? requestJson.getString("uuid") : null,
					requestJson.getString("product"), 
					requestJson.has("ssoToken") ? requestJson.getString("ssoToken") : null,
					requestJson.has("additionalPayment") ? requestJson.getString("additionalPayment") : null );
			
			LobDetailResponse lobDetailRS = lobDetailsService.fetchItineraryDetailsFromLob(lobDetailRQ);
			if(lobDetailRS.getResponseStatus() == ResponseStatus.SUCCESS || lobDetailRS.getFailureCode() != FailureCode.FETCH_ITINERARY_REQUIRED_PARAMS_MISSING)
				return new JSONObject(lobDetailRS.getRawResponse());
			
		} catch (Exception ex){
			logger.error(PAYMENT_INFO_API + ":" + FETCH_ITINERARY_EXCEPTION + " for superPnr : " + superPnr, ex);
		}
		return new JSONObject();
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		StringBuilder bufferedError = new StringBuilder();
		if(!requestJson.has("product")) 
			bufferedError.append("product not present");
		if(!requestJson.has("superPnr")) 
			bufferedError.append("superPnr not present");
		
		String error = null;
		error = bufferedError.toString();
		
		if(!YatraUtil.isNullOrEmpty(error)) {
			JSONObject errorJson = new JSONObject();
			try {
				return errorJson.put("error", error);
			} catch (JSONException e) {
				logger.error("Error while validating mandatory parameters ",e);
			}
		}
		return null;
	}

	@Override
	public String getResultKey() {
		return BOOKING_DETAILS_KEY;
	}

}
