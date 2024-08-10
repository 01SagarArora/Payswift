package com.yatra.payment.ui.service.impl;

import java.util.HashMap;

import com.yatra.payment.ui.util.MiniServiceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.platform.commons.utils.Timeit;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

import static com.yatra.payment.ui.util.MiniServiceUtil.errorJson;

@Component
public class BookingAmountServiceImpl implements MiniServiceI {

	Logger logger = Logger.getLogger(BookingAmountServiceImpl.class);
	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		try{
			throw new UnsupportedOperationException("bookingAmount service request is no longer supported.");
//			logger.debug("Getting required Data forBooking Amount Node .");
//			logger.debug("Mandatory Parameter for this service is pricing ID .");
//			Timeit.timeIt();
//			JSONObject resultJSON = validateMandatoryParameters(requestJson);
//			if(resultJSON.length() >0){
//				return resultJSON;
//			}
//			String url = YatraUtil.getPropertyManager().getProperty("booking.amount.endPoint");
//			HttpEndPoint endPoint = new HttpEndPoint(url,"GET");
//			String princingId = requestJson.getString("pricingId");
//			HashMap<String,String> parameters = new HashMap();
//			logger.debug("Hitting B2B service with following pricing ID " + princingId );
//			parameters.put("pricingId", princingId);
//			HttpService service = new HttpService();
//			String response = service.invoke(endPoint , parameters);
//			logger.debug("Response received from B2B passthrough service " + response );
//			return new JSONObject(response);
		}
		catch(Exception ex){
			logger.error("An error occurred", ex);
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
		} catch (JSONException e) {
			logger.error(e);
		}
		return validationJson;
	}

	@Override
	public String getResultKey() {
		return "bookingAmount";
	}

}
