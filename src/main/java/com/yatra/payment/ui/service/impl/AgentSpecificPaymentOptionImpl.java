package com.yatra.payment.ui.service.impl;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.payswift.beans.PaymentPageResponse;
import com.yatra.payment.payswift.beans.PaymentRequest;
import com.yatra.payment.ui.controller.PaymentUIController;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.CorporateCardPaymentOption;
import com.yatra.payment.ui.display.beans.CreditCardPaymentOption;
import com.yatra.payment.ui.display.beans.DebitCardPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentDiv;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.enums.Status;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.yatra.payment.ui.util.MiniServiceUtil.*;

@Service
public class AgentSpecificPaymentOptionImpl implements MiniServiceI {

	private Logger logger = Logger.getLogger(AgentSpecificPaymentOptionImpl.class);
	@Autowired
	private UICacheManager<PaymentDiv> paymentDivCache;
	@Autowired
	private AgentProfileService agentProfileService;
	@Autowired
	private PaymentUIController paymentUIController;
	@Autowired
	private PaymentUIHelper paymentUIHelper;
	@Autowired
	private FetchItineraryDetailsServiceImpl fetchItineraryDetailsServiceImpl;

	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		JSONObject errorJson = validateMandatoryParameters(requestJson);
		JSONObject resultJSON = new JSONObject();
		logger.debug("Now generating Agent specific payment Options . ");
		if(errorJson!= null)
			return errorJson;
		String superPnr = null;
		String product = null;
		try {
			superPnr = requestJson.getString("superPnr");
			product = requestJson.getString("product");
			String bookingType = paymentUIHelper.getBookingType(requestJson.getString("agentProfileType"));
			boolean isCrpProduct = paymentUIController.isCrpProduct(product);

			List<PaymentOption> newPaymentOptions = new ArrayList<PaymentOption>();

			List<PaymentOption> availablePaymentOptions = fetchAvailablePaymentOptions(requestJson.getString("merchant"), product);

			net.sf.json.JSONObject agentPaymentOptionJSON = new net.sf.json.JSONObject();
			JSONObject agentPaymentOptions;
			if (responseJSON.has(BOOKING_DETAILS_KEY) && !responseJSON.isNull(BOOKING_DETAILS_KEY)) {
				 agentPaymentOptions = responseJSON.getJSONObject(BOOKING_DETAILS_KEY);
			} else {
				agentPaymentOptions = fetchItineraryDetailsServiceImpl.getRequiredData(requestJson, responseJSON);
			}
			String entity = agentPaymentOptions.optString(PaymentUIUtil.ENTITY, agentPaymentOptions.optString(PaymentUIUtil.MERCHANT_CODE, null));
			boolean passthrough = agentPaymentOptions.optBoolean(PaymentUIUtil.ISPASSTHROUGH, false);
            agentPaymentOptionJSON.accumulate("agentPaymentOptions", agentPaymentOptions.getJSONObject("agentPaymentOptions").toString());
            agentPaymentOptionJSON = agentPaymentOptionJSON.getJSONObject("agentPaymentOptions");
            List<String> paymentOptions = paymentUIController.getPaymentOptionsListFromJSON(agentPaymentOptionJSON, bookingType, isCrpProduct, entity, product, passthrough);

			ObjectWriter ow = new ObjectMapper().viewWriter(PaymentOption.class);
			for(String paymentOption : paymentOptions){
				for(PaymentOption po : availablePaymentOptions){
					if(po.getCode().equalsIgnoreCase(paymentOption)){
						if(po.getCode().equalsIgnoreCase("corpCard")){
							CorporateCardPaymentOption corpCardPaymentOption = new CorporateCardPaymentOption();
							corpCardPaymentOption.setCode("corpCard");
							corpCardPaymentOption.setIframeEnabled("true");
							corpCardPaymentOption.setDisplayText("Corporate card");
							corpCardPaymentOption.setStatus(Status.ENABLED);
							corpCardPaymentOption.setIframeEnabled("true");
							corpCardPaymentOption.setMessages(null);
							corpCardPaymentOption.setPriority(2);
							newPaymentOptions.add(corpCardPaymentOption);
						}
						else{
							newPaymentOptions.add(po);
						}
						continue;
					}
				}
			}

			for(PaymentOption paymentOption: newPaymentOptions) {
				String paymentOptionJson = ow.writeValueAsString(paymentOption);
				resultJSON.accumulate(paymentOption.getCode(),new JSONObject(paymentOptionJson));
			}
			
			addSkipOtpFlagIfNeeded(paymentUIHelper.getSkipOtpFlagMap(agentPaymentOptionJSON, bookingType, isCrpProduct, entity, product, passthrough), resultJSON);

		} catch (Exception e) {
			logger.error("Caught an Exception at getAgenSpecificPaymentOption on superPnr "+superPnr+"  due to {}", e);
			return errorJson(e);
		}
		return resultJSON;
	}

	private void addSkipOtpFlagIfNeeded(Map<String, String> skipOtpFlagMap, JSONObject resultJSON) throws JSONException{
		String skipOtpFlagPayments = paymentUIHelper.getSkipOtpFlagPayments();
		String payop;
		for(String key : skipOtpFlagMap.keySet()) {
			String combinedFlag = String.valueOf(("true".equalsIgnoreCase(skipOtpFlagMap.getOrDefault(key, "false")) && "true".equalsIgnoreCase(skipOtpFlagPayments)));
			if(key.equalsIgnoreCase("corpcard"))
				payop = "corpCard";
			else payop = key;
			if(resultJSON.has(payop))		//added to handle UPI hardcoding changes
				resultJSON.getJSONObject(payop).put(PaymentUIUtil.SKIP_OTP_FLAG, combinedFlag);
		}

	}
	
	private void appendSurchargeNode(JSONObject resultJSON, net.sf.json.JSONObject agentPaymentOptionJSON) {
		try {
		logger.debug("Appending surcharge Details ");
		net.sf.json.JSONObject surchargeJSON = agentPaymentOptionJSON.getJSONObject("surcharge");
		logger.debug("surcharge JSON found " + surchargeJSON);
			resultJSON.accumulate("surcharge", surchargeJSON);
			// TODO Auto-generated catch block
		} catch (JSONException e) {
			logger.error(e);
		}
	}

	private List<PaymentOption> fetchAvailablePaymentOptions(String merchant,String product) {
		logger.debug("Fetching available payment Options for merchant and product "+merchant+" , "+product);
		String key = PaymentUIUtil.getPaymentDivKey(merchant, product);
		PaymentDiv paymentDiv = paymentDivCache.get(key);
		logger.info("Payment DIV received from cache : "+paymentDiv == null ?  "false":"true");
		return paymentDiv.getPaymentOptions();
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
		if (!requestJson.has("agentProfileType"))
			bufferedError.append("agentProfileType not present");

		String error = null;
		error = bufferedError.toString();

		if (!YatraUtil.isNullOrEmpty(error)) {
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
		return PAYMENT_OPTION_KEY;
	}
	

}
