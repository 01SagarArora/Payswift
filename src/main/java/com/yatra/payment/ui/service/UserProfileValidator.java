package com.yatra.payment.ui.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("userProfileValidator")
public class UserProfileValidator extends Validator {
	
	public String validateGetUserProfile(Map<String, String> requestMap, String ssoToken) {
		
		String email = requestMap.get(PaymentUIUtil.EMAIL);
		String merchantCode = requestMap.get(PaymentUIUtil.MERCHANT_CODE);
		String productCode = requestMap.get(PaymentUIUtil.PRODUCT_CODE);
		
		String error = validateSSOToken(ssoToken);
		error += validateEmail(email, ssoToken);
		error += validateMerchantCode(merchantCode);
		error += validateProductCode(productCode);
		return error;
	}
}
