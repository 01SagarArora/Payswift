package com.yatra.payment.ui.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.bean.Currency;
import com.yatra.payment.ui.dao.bean.UIMerchantCurrencyMapping;
import com.yatra.payment.ui.service.CurrencyConfigCache;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

@Service
public class CurrencyConversionServiceImpl implements MiniServiceI {
	
	Logger logger = Logger.getLogger(CurrencyConversionServiceImpl.class);
	
	@Autowired private PropertyManager propertyManager;
	@Autowired private HttpService httpService;
	@Autowired private StatusProviderService statusProviderService;
	@Autowired private CurrencyConfigCache currencyConfigCache;
	
	public static List<Currency> currencyList = new ArrayList();
	public static final String GENERIC_ERROR_MESSAGE = "Generic exception while fetching currencty conversions.";
	public static final String PAYOP_NOT_ENABLED = "Paypal is not enabled for the product.";

	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		String superPNR = null;
		JSONObject errorJson = validateMandatoryParameters(requestJson);
		if(errorJson!= null) return errorJson;
		try{
			superPNR = requestJson.getString("superPnr");
			String merchant = requestJson.getString("merchant");
			String product = requestJson.getString("product");
			
			Boolean isPaypalEnabled = statusProviderService.isPaymentOptionEnabled( merchant, product, PaymentUIUtil.PAYMENT_OPTION_PAYPAL);
			if(isPaypalEnabled == null || !isPaypalEnabled){
				logger.debug("Paypal is not enabled for the product : " + product + " and merchnat : " + merchant);
				return getErrorJson(CurrencyConversionServiceImpl.PAYOP_NOT_ENABLED);
			}
			String currencyCodes = getPipeSeperatedCurrenyCodes();
			String url = propertyManager.getProperty("non.secure.get.currency.conversion.service");
			logger.info("Hitting URL : " + url + " with parameters, merchant_code : " + merchant + ", product :" + product + " and sourceCurrency : " + currencyCodes + " for superPNR : " + superPNR);
			HttpEndPoint endPoint = new HttpEndPoint(url, "GET");
			String response = httpService.invoke(
								endPoint, 
								new HashMap<String, String>()
									{{
										 put("merchant_code", merchant);
										 put("product_code", product);
										 put("sourceCurrency", currencyCodes);
									}}
								);
			logger.debug("Received response from currency conversion service for superPNR : " + superPNR + ": " + response );
			return postProcessResponse(new JSONObject(response), currencyConfigCache.getEnabledCurrencies(merchant, product));
		} catch(Exception ex) {
			logger.error("Error while getting currenct conversion for superPNR : " + superPNR + " , Exception is : ", ex);
			return getErrorJson(GENERIC_ERROR_MESSAGE);
		}
	}

	private JSONObject postProcessResponse(JSONObject currencyJSON, List<UIMerchantCurrencyMapping> enabledCurrencies) {
		try {
			JSONObject finalResponse  = new JSONObject();
			if(enabledCurrencies == null || enabledCurrencies.isEmpty() || !"true".equals(currencyJSON.getString("status"))){
				logger.error("Error in response or Currency Config Not defined. Returning orignal response");
				return currencyJSON;
			}
			
			String defaultCurrency = null;
			JSONArray sortedCurrencyArray = new JSONArray();
			JSONArray orignalCurrencyArray = currencyJSON.getJSONArray("currencies");
			
			for(UIMerchantCurrencyMapping config : enabledCurrencies) {
				for(int i = 0; i < orignalCurrencyArray.length(); i++) {
					JSONObject currency = orignalCurrencyArray.getJSONObject(i);
					if(currency.getString("code").equals(config.getCurrencyCode())) {
						sortedCurrencyArray.put(currency);
						if(defaultCurrency == null && config.isDefault())
							defaultCurrency = config.getCurrencyCode();
					}
				}
			}
			finalResponse.put("status", "true");
			finalResponse.put("defaultCurrency", defaultCurrency);
			finalResponse.put("currencies", sortedCurrencyArray);
			
			return finalResponse;
			
		} catch (Exception e) {
			logger.error("Error while post processing currencies JSON, returning orignal resonse.");
			return currencyJSON;
		}
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		StringBuilder bufferedError = new StringBuilder();
		if(!requestJson.has("product")) 
			bufferedError.append("product not present");
		if(!requestJson.has("merchant")) 
			bufferedError.append("merchant not present");
		
		String error = null;
		error = bufferedError.toString();
		
		if(!YatraUtil.isNullOrEmpty(error)) {
			return getErrorJson(error);
		}
		return null;
	}

	@Override
	public String getResultKey() {
		return "currencyConversionJSON";
	}
	
	public JSONObject getErrorJson(String errorMessage) {
		JSONObject errorJson = new JSONObject();
		try {
			errorJson.put("status", "false");
			errorJson.put("errorMessage", errorMessage);
		} catch (JSONException ex) {
			logger.error("Error while creating error JSON for currency conversion API, Exception is : ", ex);
		}
		return errorJson;
	}

	private List<Currency> currencyList() {
		try{
			if(currencyList == null || currencyList.isEmpty()){
				currencyList = currencyConfigCache.getMasterCurrencyList();
			}
		} catch (Exception ex){
			logger.error("Error while fetching enabled currencies, Exception is :", ex);
			currencyList =  new ArrayList<>();
		}
		return currencyList;
	}
	
	private String getPipeSeperatedCurrenyCodes() {
		StringBuilder currencyStringBuilder =  new StringBuilder("");
		for(Currency currency : currencyList()){
			currencyStringBuilder.append(currency.getCode()).append('|');
		}
		return currencyStringBuilder.toString();
	}
	
	public void clearCurrencyList() {
		logger.debug("Going to clear currency list.");
		currencyConfigCache.buildNewCache();
		this.currencyList().clear();
		logger.debug("Currency list cleared !!");
	}
	
	public JSONObject getCurrencyConversionAPIRequest(String product, String merchant, String superPnr) {
		JSONObject jsonObject = new JSONObject();
		try{
			jsonObject.put("product", product);
			jsonObject.put("merchant", merchant);
			jsonObject.put("superPnr", superPnr);
		} catch (Exception ex) {
			logger.error("Error while creating currency conversion request");
		}
		return jsonObject;
	}
}
