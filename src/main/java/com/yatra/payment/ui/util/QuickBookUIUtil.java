package com.yatra.payment.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.yatra.tokenized.cards.v3.beans.ResponseStatus;


public class QuickBookUIUtil {
	
	//ExpressCheckout Service Error Codes
	public static final String RE_AUTH_CODE = "112";
	public static final String RE_LOGIN_CODE = "103";
	public static final String NO_CARDS_CODE = "106";
	
	//ExpressCheckout Service Card Types
	public static final String CARD_TYPE_CREDIT_CARD = "CreditCard";
	public static final String CARD_TYPE_CREDIT_CARD_2 = "Credit Card";
	public static final String CARD_TYPE_DEBIT_CARD = "DebitCard";
	public static final String CARD_TYPE_DEBIT_CARD_2 = "Debit Card";
	
	//Card Type Codes
	public static final String CREDIT_CARD_CODE = "cc";
	public static final String DEBIT_CARD_CODE = "dc";
	
	//ExpressCheckout Service Card Brands
	public static final String CARD_BRAND_VISA = "VISA";
	public static final String CARD_BRAND_MASTER = "MASTER";
	public static final String CARD_BRAND_AMEX = "AMEX";
	public static final String CARD_BRAND_DINERS = "DINERS";
	public static final String CARD_BRAND_MAESTRO = "MAESTRO";

	public static final String CARD_BRAND_RUPAY = "RUPAY";
	
	//Card Brand Codes
	public static final String VISA_CODE = "VISA";
	public static final String MASTER_CODE = "MASTER";
	public static final String AMEX_CODE = "AMEX";
	public static final String DINERS_CODE = "DINERS";
	public static final String MAESTRO_CODE = "MAEST";
	public static final String RUPAY_CODE = "RUPAY";
	
	//Cookies
	public static final String SSO_TOKEN_COOKIE = "ssoToken";
	public static final String CARD_HOLDER_TITLE = "Mr.";
	
	//QB Facebook Authentication
	public static final String FB_SUCCESS_LOGIN_CODE = "104";
	
	//QB AUTH MODES
	public static final String AUTH_MODE_FB = "FACEBOOK";
	public static final String AUTH_MODE_YATRA = "YATRA";
	
	//FB RE-AUTH RELATED CONSTANTS
	public static final String RE_AUTH_MODE_FB = "FACEBOOK";
	public static final String LOGIN_STEP = "ResponseCheck";
	
	//Others
	public static final String RESPONSE_CODE_KEY = "response_code";
	public static final String SSO_TOKEN_KEY = "sso_token";
	public static final String USER_NAME_COOKIE = "userName";
	public static final String UTF_CHAR_SET = "UTF-8";
	
	// Quick Book Card deletion specific constants.
	public static final String QB_DELETE_CARD_SUCCESS_CODE = "SUCCESS";
	public static final String QB_DELETE_CARD_FAILURE_CODE = "FAILURE";
	
	// Default time out for QB fetch.
	public static final long DEFAULT_TIMEOUT_FOR_QB_FETCH = 5;

	// QB Save card constants.
	public static final String CARD_ID = "cardId";
	public static final String CARD_ACCESS_TYPE_INT = "INT";
	public static final String CARD_ACCESS_TYPE_DOM = "DOM";
	public static final String CARD_BRAND = "cbrand";
	public static final String CARD_NAME = "cname";
	public static final String PRODUCT_GROUP = "productGroup";
	public static final String LEVEL_GROUP = "levelGroup";
	public static final String TARGET = "target";
	
	// Status
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAILURE = "FAILURE";
	
	// Error codes
	public static final String ERROR_CODE_EXCEPTION = "EXCEPTION";
	public static final String ERROR_CODE_INVALID = "INVALID";
	public static final String ERROR_CODE_NULL_RESPONSE = "NULL_RESPONSE";
	public static final String ERROR_CODE_AUTH_REQD = "AUTH_REQUIRED";
	public static final String ERROR_CODE_NOT_BEAN_EXISTS = "NO_BEAN_EXISTS";
	
	// Error Msg
	public static final String ERROR_MSG_NULL_RESPONSE = "Null Response received";
	public static final String ERROR_MSG_AUTH_REQD = "Authentication is required. Login expired.";
	public static final String TRAVELLER_SSO_USER_ID_ABSENT = "travellerSSOId is absent.";
	
	public static String getResponseJSON(boolean responseStatus) {
		String status = responseStatus ? STATUS_SUCCESS : STATUS_FAILURE;
		return getResponseJSON(status, null, null,null);
	}
	
	public static String getResponseJSON(ResponseStatus responseStatus) {
		if(responseStatus == null)
			return getNullResponseJSON();
		
		String status = StringUtils.equals(responseStatus.getStatus(), STATUS_SUCCESS) ? STATUS_SUCCESS : STATUS_FAILURE;
		String errorCode = responseStatus.getErrorCode();
		String errorMsg = responseStatus.getErrorMessage();
		String cardId = responseStatus.getCardId();
		return getResponseJSON(status, errorCode, errorMsg,cardId);
	}

	/* for quick book cards */
	public static String getResponseJSON(com.yatra.express.cards.v3.beans.ResponseStatus responseStatus) {
		if(responseStatus == null)
			return getNullResponseJSON();

		String status = StringUtils.equals(responseStatus.getStatus(), STATUS_SUCCESS) ? STATUS_SUCCESS : STATUS_FAILURE;
		String errorCode = responseStatus.getErrorCode();
		String errorMsg = responseStatus.getErrorMessage();
		String cardId = responseStatus.getCardId();
		return getResponseJSON(status, errorCode, errorMsg,cardId);
	}



	public static String getResponseJSON(String status, String errorCode, String errorMsg, String cardId) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("status", status);
		jsonMap.put("error_code", errorCode);
		jsonMap.put("error_msg", errorMsg);
		jsonMap.put("cardId", cardId);
		return new JSONObject(jsonMap).toString();
	}
	
	public static String getAuthRequiredJSON() {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_AUTH_REQD, ERROR_MSG_AUTH_REQD,null);
	}
	
	public static String getNullResponseJSON() {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_NULL_RESPONSE, ERROR_MSG_NULL_RESPONSE,null);
	}
	
	public static String getExceptionJSON(String exceptionMsg) {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_EXCEPTION, exceptionMsg,null);
	}
	
	public static String getInvalidRequestJSON(String invalidErrorMsg) {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_INVALID, invalidErrorMsg,null);
	}

}
