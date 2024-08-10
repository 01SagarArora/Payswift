package com.yatra.payment.ui.payswift.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

@Service
public class CrpAutoBookPayOpService {
	
	
	private static final Logger logger = Logger.getLogger(CrpAutoBookPayOpService.class);
	private static final JSONArray CACHED_CONFIG_ARR;
	private static final Map<String, String> PAYOP_NAME_MAPPING;
	
	private static final String PAYOP_URL_KEY = "corporate.auto.book.payment.option.endpoint";
	
	@Autowired PropertyManager properties;
	@Autowired HttpService httpService;
	
	static {
		CACHED_CONFIG_ARR = buildConfigsArrForPayopApi();
		
		PAYOP_NAME_MAPPING = new HashMap<>();
		PAYOP_NAME_MAPPING.put(PaymentUIUtil.CP, PaymentUIUtil.PAYMENT_OPTION_CREDIT_POOL);
		PAYOP_NAME_MAPPING.put(PaymentUIUtil.CORPCARD, PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD);
		
	}
	
	public String getPayOpRes(String ssoToken , String txnId) throws Exception{
		 
		String url = properties.getProperty(PAYOP_URL_KEY);
		String requestJson = buildReqJson(ssoToken , txnId);
		logger.debug("Hitting corporate get-payment-option endpoint with params :" + requestJson );
	    HttpEndPoint serviceEndPoint = new HttpEndPoint(url,"POST");
	    String paymentOptions = httpService.invoke(serviceEndPoint, null,null, requestJson);
	    logger.info("corporate get-payment-option service response : " + paymentOptions);
	
		return paymentOptions;
	}
	
	private String buildReqJson(String ssoToken , String txnId) throws Exception{
		
		JSONObject reqJson = new JSONObject();
		reqJson.put("ssoToken", ssoToken);
		reqJson.put(PaymentUIUtil.TXN_ID, txnId);
		reqJson.put("configs", CACHED_CONFIG_ARR);
		
		return reqJson.toString();
	}
	
	public List<String> getPayOpsInpriorityOrder(JSONObject corporatePaymentJson, String bookingType, String productType){
		
		try {
			JSONArray payopsJson = corporatePaymentJson.getJSONObject("configurations").getJSONObject("paymentOptions")
								.getJSONObject(productType).getJSONArray(bookingType);
			
			List<Payop> payopList = new ArrayList<>();
			
			for(int i=0; i<payopsJson.length(); i++){
				JSONObject obj = payopsJson.getJSONObject(i);
				if(obj.has(PaymentUIUtil.CORPCARD) || obj.has(PaymentUIUtil.CP)){
					String payopName = obj.has(PaymentUIUtil.CORPCARD) ? PaymentUIUtil.CORPCARD : PaymentUIUtil.CP ;
					if(obj.getJSONObject(payopName).has(PaymentUIUtil.AUTO_BOOK_PRIORITY_NODE_KEY)){
						String paymentOption = PAYOP_NAME_MAPPING.get(payopName);
						payopList.add(new Payop(paymentOption,obj.getJSONObject(payopName).getInt(PaymentUIUtil.AUTO_BOOK_PRIORITY_NODE_KEY)));
					}
				}
			}
			Collections.sort(payopList);
			
			List<String> finalResultList = new ArrayList<>();
			for(Payop payop : payopList){
				finalResultList.add(payop.name);
			}
			return finalResultList;
			
		} catch (Exception e) {
			logger.error("Error while parsing payment-option json ",e);
			return Collections.<String>emptyList();
		}
		
	}
	
	private static JSONArray buildConfigsArrForPayopApi(){
		
		JSONArray configArr = new JSONArray();
		try{
			JSONObject whereConditionArrElem1 = new JSONObject();
			whereConditionArrElem1.put("name", "channel");
			whereConditionArrElem1.put("value", "web");
			
			JSONArray whereConditionArr = new JSONArray();
			whereConditionArr.put(whereConditionArrElem1);
			
			JSONObject configArrElem1 = new JSONObject();
			configArrElem1.put("name", "paymentOptions");
			configArrElem1.put("whereConditions", whereConditionArr);
			
			configArr.put(configArrElem1);
		}catch(Exception e) { // never happen
			logger.error("Fatal error , corporate-auto-book would not work!!!",e); 
		}
		
		return configArr;
		
	}
	
	static class Payop implements Comparable<Payop>{
		
		String name;
		int priority;
		
		public Payop(String name, int priority) {
			this.name = name;
			this.priority = priority;
		}
		
		@Override
		public int compareTo(Payop that) {
			return this.priority - that.priority;
		}
		
	}
	

}
