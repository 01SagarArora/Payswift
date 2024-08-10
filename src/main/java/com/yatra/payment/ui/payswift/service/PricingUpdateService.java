package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.GSTUpdateRequest;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

@Service
public class PricingUpdateService {

	@Autowired private LoggingProcessor loggingProcessor;
	@Autowired private HttpService httpService;
	Logger logger = Logger.getLogger(PromoCodeService.class);
	public String getUpdatedFareBreakUp(GSTUpdateRequest gstUpdateRequest)  {
		String response = null;
		try {
			Map<String,String> parameterMap = populateParameterMap(gstUpdateRequest);
			logger.info("Hitting UpdateGST Service for fetching updatedFareBreakUp");

			String gstUpdateUrl = gstUpdateRequest.getUpdateGSTUrl();
			String postdata = gstUpdateUrl + "?" + PaymentUIUtil.createPostDataFromMap(parameterMap);
			loggingProcessor.logIntoPayswiftStages(gstUpdateRequest.getSuperPnr(), PaymentUIUtil.GST_UPDATE_REQ, postdata, gstUpdateRequest.getProduct());
			if (!StringUtils.isBlank(gstUpdateUrl)) {
				response = sendPostAccToProduct(gstUpdateUrl, parameterMap, gstUpdateRequest.getProduct());
			}
			else logger.info("GST Url not present in request");
			logger.info("Response received from LOB are " + response);
			loggingProcessor.logIntoPayswiftStages(gstUpdateRequest.getSuperPnr(), PaymentUIUtil.GST_UPDATE_RES, response, gstUpdateRequest.getProduct());

		} catch (Exception ex) {
			logger.error("An error occurred while sending request to LOB ");
		}

		if(!YatraUtil.isNullOrEmpty(response)) {
			try {
				JSONObject respJson = new JSONObject(response);
				if (respJson.has("gst") && (respJson.getJSONObject("gst")).has("totalgstAmount")) {
					return respJson.getJSONObject("gst").getString("totalgstAmount");
				}
			}
			catch(JSONException e) {
				logger.info("Exception while converting response to JSON", e);
			}
		}

		return response;
	}

	private Map<String,String> populateParameterMap(GSTUpdateRequest gstUpdateRequest) {
		Map<String,String> parameterMap = new HashMap();
		parameterMap.put(PaymentUIUtil.TOTAL_DISCOUNT,gstUpdateRequest.getTotalDiscount());
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,gstUpdateRequest.getPaymentMode());
		return parameterMap;
	}

	public String sendPostAccToProduct(String gstUpdateUrl, Map<String, String> parameterMap, String productCode) {
		String responseJSON = "";
		try {
			if (productCode.equals(PaymentUIUtil.PRODUCT_B2B) || productCode.equals(PaymentUIUtil.PRODUCT_CORPORATE)) {
				HttpEndPoint endPoint = new HttpEndPoint(gstUpdateUrl, "GET");
				responseJSON = httpService.invoke(endPoint, parameterMap);
			} else {
				HttpEndPoint endPoint = new HttpEndPoint(gstUpdateUrl, "POST");
				responseJSON = httpService.invoke(endPoint, parameterMap);
			}
		} catch (Exception ex) {
			logger.error("An error ocurred while fetching Updated GST Details ", ex);
			return "";

		}
		return responseJSON;
	}


}
