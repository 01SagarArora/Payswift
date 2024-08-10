package com.yatra.payment.client.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.utils.PaymentClientUtil;
import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.platform.commons.property.PropertyManager;

@Service("paymentHandshakeService")
public class PaymentHandshakeService {
	
	private static Logger logger = Logger.getLogger(PaymentHandshakeService.class);
	
	@Autowired private HttpUtil httpUtil;
	@Autowired	private PropertyManager  yatraPropertyReader;
	
	public String handshake(String merchantCode, String productCode, String ttid, String merchantTxnRefId, String walletId) throws Exception {
		
		boolean isRequestInvalid = StringUtils.isEmpty(merchantCode) || StringUtils.isEmpty(productCode)
				|| (StringUtils.isEmpty(ttid) && StringUtils.isEmpty(merchantTxnRefId) && StringUtils.isEmpty(walletId));
		if (isRequestInvalid) {
			logger.error("Invalid handshake request.. Request parameters cannot be null. merchantCode : " + merchantCode + ", productCode : " + productCode + ", ttid : " + ttid + ", merchantTxnRefId : " + merchantTxnRefId + " and walletId : " + walletId);
			throw new Exception("Invalid handshake request.. Request parameters cannot be null. merchantCode : " + merchantCode + ", productCode : " + productCode + ", ttid : " + ttid + ", merchantTxnRefId : " + merchantTxnRefId + " and walletId : " + walletId);
		}
		
		// Get Handshake URL.
		String url = getHandshakeURL();
		
		// Create Post request handshake data.
		Map<String, String> requestMap = createRequestMap(merchantCode, productCode, ttid, merchantTxnRefId, walletId);
		String postData = httpUtil.createPostDataFromMap(requestMap);
		
		// Post request on handshake URL.
		return httpUtil.invoke(url, postData, "PAYMENT_HANDSHAKE");
	}
	
	private String getHandshakeURL() {
		String secureHandshakeURL = yatraPropertyReader.getProperty("secure.payment.handshake.url");
		String nonSecureHandshakeURL = yatraPropertyReader.getProperty("non.secure.payment.handshake.url");
		return PaymentClientUtil.getRequestURL(secureHandshakeURL, nonSecureHandshakeURL);
	}
	
	private Map<String, String> createRequestMap(String merchantCode, String productCode, String ttid, String merchantTxnRefId, String walletId) {
		Map<String, String> requestMap = new HashMap<String, String>();
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.MERCHANT_CODE, merchantCode);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.PRODUCT_CODE, productCode);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.TRIP_ID, ttid);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.MTXN_REF_ID, merchantTxnRefId);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.WALLET_ID, walletId);
		return requestMap;
	}
}
