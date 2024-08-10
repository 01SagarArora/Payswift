package com.yatra.payment.ui.payswift.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

@Service
public class IxigoMoneyService {
	
	private static Logger logger = Logger.getLogger(IxigoMoneyService.class);
	
	@Autowired private PropertyManager propertyManager;
	@Autowired private HttpService httpService;
	@Autowired private PaymentUIHelper paymentUIHelper;
	
	private static final String SERVICE_NAME_REDEEM_IXIGO_MONEY = "REDEEM_IXIGO_MONEY";
	
public JSONObject redeemIxigoMoney(Map<String, String> portalMap, String merchant, String product) throws Exception {
		
		String url = propertyManager.getProperty("ixigomoney.service.redeem.endpoint");
		
		logger.debug("Hitting " + SERVICE_NAME_REDEEM_IXIGO_MONEY + " for superPNR : " + portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL) + " with URL: " + url + " and Request Params : " + portalMap.toString());
		paymentUIHelper.logIntoDB(portalMap.toString(), "REQUEST", portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL), SERVICE_NAME_REDEEM_IXIGO_MONEY);
		
		HttpEndPoint endPoint = new HttpEndPoint(url, "POST");
		String response = httpService.invoke(endPoint, portalMap);
		
		logger.debug(SERVICE_NAME_REDEEM_IXIGO_MONEY + " API response for superPNR : " + portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL) + " is : " + response);
		paymentUIHelper.logIntoDB(response, "RESPONSE", portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL), SERVICE_NAME_REDEEM_IXIGO_MONEY);
		
		return new JSONObject(response);
	}

}
