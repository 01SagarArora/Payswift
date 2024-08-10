package com.yatra.payment.ui.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class Validator {
	
	private static Logger logger = Logger.getLogger(Validator.class);
	
	@Autowired protected StatusProviderService statusProviderService;
	@Autowired protected UserLoginService userLoginService; 
	
	protected String validateSSOToken(String ssoToken) {
		String error = "";
		if (StringUtils.isBlank(ssoToken)) 
			error = "SSO token not present. ";
		return error;
	}
	
	protected String validateMerchantCode(String merchantCode) {
		String error = "";
		if (StringUtils.isBlank(merchantCode)) 
			error = "Merchant Code is not present. ";
		return error;
	}
	
	protected String validateProductCode(String productCode) {
		String error = "";
		if (StringUtils.isBlank(productCode)) 
			error = "Product Code is not present. ";
		return error;
	}
	
	protected String validateEmail(String email) {
		String error = "";
		if (StringUtils.isBlank(email))
			error = "Email Id is blank. ";
		return error;
	}
	
	protected String validateEmail(String email, String ssoToken) {
		
		String error = validateEmail(email);
		
		if (StringUtils.isEmpty(error) && StringUtils.isEmpty(validateSSOToken(ssoToken))) {
			
			// Get email from User Profile Service and validate.
			String userEmail = userLoginService.getEmailForSSOToken(ssoToken);
	
			if(!StringUtils.equalsIgnoreCase(email, userEmail)) {
				logger.error("Emails do not match for ssoToken : " + ssoToken + ". Email received for payment : " + email + " and email received from User Profile Service : " + userEmail);
				error = "Email Id is incorrect. ";
			}
		}
		return error;
	}
	
	protected String validatePayOpEnabled(String merchantCode, String productCode, String payOpCode) {
		
		String error = "";
		
		// Check if merchantCode and productCode are not blank.
		boolean isMerchantCodeValid = StringUtils.isEmpty(validateMerchantCode(merchantCode));
		boolean isProductCodeValid = StringUtils.isEmpty(validateProductCode(productCode));
		
		if(isMerchantCodeValid && isProductCodeValid) {
			
			// Check if payOp is enabled.
			Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(payOpCode);
			logger.info("Is payment option with code : " + payOpCode + " enabled : " + isPayOpEnabled);
			
			if(isPayOpEnabled == null || !isPayOpEnabled) 
				return "Payment Option " + payOpCode + " is not enabled.";
			
			// Check if payOp is enabled for given merchant and product.
			Boolean isPayOpEnabledForProduct = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, payOpCode);
			logger.info("Is Payment Option with code : " + payOpCode + " enabled for merchant : " + merchantCode + " and productCode : " + productCode + " : " + isPayOpEnabledForProduct);
			
			if(isPayOpEnabledForProduct == null || !isPayOpEnabledForProduct) 
				return "Payment Option " + payOpCode + " is not enabled for merchant : " + merchantCode + " and product : " + productCode + ". ";
			
		}
		return error;
	}
}
