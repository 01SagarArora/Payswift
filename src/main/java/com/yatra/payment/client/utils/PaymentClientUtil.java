package com.yatra.payment.client.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.yatra.payment.client.enums.PaymentOption;

public class PaymentClientUtil {

	private static Logger logger = Logger.getLogger(PaymentClientUtil.class);
	
	public static PaymentOption getPaymentOption(Map<String, String> paymentParamsMap) throws Exception {
		try{
			String paymentOption = paymentParamsMap.get(PaymentConstants.PAYMENT_OPTION_PARAM);
			return PaymentOption.valueOf(paymentOption.toUpperCase());
		} catch (Exception e) {
			logger.error("Exception occurred while determining payment option chosen by customer : " + e);
			throw new Exception("Exception occurred while determining payment option chosen by customer : " + e);
		}
	}
	
	public static Map<String, String> convertStringToMap(String paramString, String separator) throws Exception {
		String paramArray[] = paramString.split(separator);
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : paramArray) {
			String paramName = "";
			String paramValue = "";
			try {
				paramName = param.split("=")[0];
				paramValue = param.split("=")[1];
			} catch (Exception e) {}
			paramsMap.put(paramName, paramValue);
		}
		return paramsMap;
	}
	
	

	public static void populateMap(Map<String, String> map, String key, String value) {
		if(StringUtils.isNotEmpty(value))
			map.put(key, value);
	}
	
	public static String getRequestURL(String secureURL, String nonSecureURL) {
		return nonSecureURL;
	}
	
	public static String getKey(String merchant, String product) {
		StringBuilder cacheKey = new StringBuilder();
		return cacheKey.append(merchant).append("_").append(product).toString();
	}
}
