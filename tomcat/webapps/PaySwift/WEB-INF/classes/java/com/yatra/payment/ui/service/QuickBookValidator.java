package com.yatra.payment.ui.service;

import java.util.Map;

import com.yatra.payment.ui.exceptions.PayswiftException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;

import static com.yatra.payment.ui.enums.FailureCode.GET_QB_CARDS_REQUEST_INVALID;

@Service("quickBookValidator")
public class QuickBookValidator extends Validator {
	
	private static Logger logger = Logger.getLogger(QuickBookValidator.class);
	
	public void validateGetQBCardsRequest(String merchantCode, String productCode, String ssoToken) {
		String error = validateSSOToken(ssoToken);
		error += validateMerchantCode(merchantCode);
		error += validateProductCode(productCode);
		error += validatePayOpEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK);

		if (StringUtils.isNotEmpty(error)) {
			throw new PayswiftException(GET_QB_CARDS_REQUEST_INVALID.getCode(), "Invalid request for getting qb cards: " + error);
		}
	}
	
	public String validatePassCompromisedRequest(String ssoToken) {
		String error = validateSSOToken(ssoToken);
		return error;
	}
	
	public String validateUpdatePassRequest(String ssoToken, Map<String, String> requestMap) {
		
		String oldPassword = requestMap.get("oldPassword");
		String newPassword = requestMap.get("newPassword");
		String confirmPassword = requestMap.get("confrmPassword");
		String email = requestMap.get("email");
		
		String error = validateSSOToken(ssoToken);
		error += validateEmail(email);
		error += validateOldPassword(oldPassword);
		error += validateNewPassword(newPassword, oldPassword);
		error += validateConfirmPassword(confirmPassword, newPassword);
		return error;
	}
	
	public String validateDeleteCardRequest(String ssoToken, Map<String, String> requestMap) {
		
		String merchantCode = requestMap.get("merchant");
		String productCode = requestMap.get("product");
		String cardId = requestMap.get("cardid");
		
		String error = validateSSOToken(ssoToken);
		error += validateMerchantCode(merchantCode);
		error += validateProductCode(productCode);
		error += validateCardId(cardId);
		return error;
	}
	
	public String validateSaveCardRequest(String ssoToken, Map<String, String> requestMap) {
		
		String merchantCode = requestMap.get("merchant");
		String productCode = requestMap.get("product");
		
		String error = validateSSOToken(ssoToken);
		error += validateMerchantCode(merchantCode);
		error += validateProductCode(productCode);
		return error;
	}
	
public String validateSaveGuestCardRequest(Map<String, String> requestMap) {
		
		String merchantCode = requestMap.get("merchant");
		String productCode = requestMap.get("product");
		String userId = requestMap.get(PaymentUIUtil.USER_ID);
		
		String error = validateMerchantCode(merchantCode);
		error += validateProductCode(productCode);
		error += validateUserId(userId);
		return error;
	}
	
	
	private String validateUserId(String userId) {
		String error = "";
		if (StringUtils.isBlank(userId))
			error = "userId is not present.";
		return error;
	}

	private String validateCardId(String cardId) {
		String error = "";
		if (StringUtils.isBlank(cardId))
			error = "cardId is not present.";
		return error;
	}
	
	private String validateOldPassword(String oldPassword) {
		String error = "";
		if (StringUtils.isBlank(oldPassword))
			error = "oldPassword is not present.";
		return error;
	}
	
	private String validateNewPassword(String newPassword, String oldPassword) {
		String error = "";
		if (StringUtils.isBlank(newPassword))
			error = "newPassword is not present.";
		
		if(StringUtils.equals(oldPassword, newPassword))
			error += "Old and new passwords are same.";
			
		return error;
	}
	
	private String validateConfirmPassword(String confirmPassword, String newPassword) {
		String error = "";
		if (StringUtils.isBlank(confirmPassword))
			error = "confirmPassword is not present.";
		
		if(!StringUtils.equals(newPassword, confirmPassword))
			error += "New Password and confirm password do not match.";
		return error;
	}

	public String isCardInternational(String cardNo) {
		String bin = "";
		try {
			if (StringUtils.isNotBlank(cardNo) && cardNo.length() > 6) 
				bin = cardNo.substring(0, 6);
			
			String url = "http://payment.service/checkout/card/get-card-type?cno="+bin;
			String resp = PaymentUIUtil.sendRequest(url, "POST");
			
			JSONObject respJson = new JSONObject(resp.toString());
			return respJson.optString("isCardInternational", "true");
		} catch(Exception e) {
			logger.error("Unable to identify card-type, so returning internationla status as true");
		}
		return "true";
	}
	
}
