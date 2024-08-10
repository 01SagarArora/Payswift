package com.yatra.payment.ui.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.wallet.beans.req.GetECashRQ;
import com.yatra.wallet.beans.resp.GetECashRS;
import com.yatra.wallet.client.WalletClient;

@Service("walletServiceClient")
public class WalletServiceClient {
	
	private static Logger logger = Logger.getLogger(WalletServiceClient.class);
	
	@Autowired WalletValidator walletValidator;
	@Autowired private TenantService tenantService;
	
	public GetECashRS getECash(Map<String, String> requestMap) {
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		try {
			logger.info("Getting eCash for user with email : " + emailId);
			
			// Validate get eCash request.
			String error = walletValidator.validateGetECashRequest(requestMap);	
			logger.info("GetECash request validation result : " + error);
			
			// If request is not valid (error is not empty), return error response.
			boolean isValid = StringUtils.isEmpty(error);
			if(!isValid) {
				logger.info("Get eCash request is invalid for email : " + emailId + ". Returning error response..");
				return getECashErrorResponse(error);				
			}
			
			// Get eCash request that is to be sent to wallet service.
			GetECashRQ getECashRQ = getECashRQ(requestMap);
			
			// Hit wallet service for get eCash.
			GetECashRS getECashRS = new WalletClient().getECash(getECashRQ);
			logger.info("GetECashRS for email : " + emailId + " and GetECashRQ : " + getECashRQ + " is : " + getECashRS);
			
			// Return Get eCash response.
			return getECashRS;
			
		} catch (Exception e) {
			logger.error("Exception occurred while getting eCash for user with email : " + emailId, e);
			return getECashErrorResponse(e.getMessage());
		}
	}
	
	private GetECashRQ getECashRQ(Map<String, String> requestMap) {
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		logger.info("Creating GetECashRQ for email : " + emailId);
		
		String merchantCode = requestMap.get(PaymentUIUtil.MERCHANT_CODE);
		String productCode = requestMap.get(PaymentUIUtil.PRODUCT_CODE);
		String tenantId = getTenantId(merchantCode, productCode);
		
		GetECashRQ getECashRQ = new GetECashRQ();
		getECashRQ.setTenantId(tenantId);
		getECashRQ.setUserEmail(emailId);
		return getECashRQ;
	}

	private String getTenantId(String merchantCode, String productCode) {
		Long tenantId = tenantService.getTenantId(merchantCode, productCode);
		return (tenantId == null) ? PaymentUIUtil.DEFAULT_TENANT_ID : String.valueOf(tenantId);
	}
	
	private GetECashRS getECashErrorResponse(String errorMsg) {
		GetECashRS getECashRS = new GetECashRS();
		getECashRS.setStatus(false);
		getECashRS.setResponseMsg(errorMsg);
		return getECashRS;
	}
}
