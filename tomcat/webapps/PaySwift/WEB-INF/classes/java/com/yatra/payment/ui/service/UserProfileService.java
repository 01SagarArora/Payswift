package com.yatra.payment.ui.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.yatra.payment.ui.util.QuickBookUIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.EWallet;
import com.yatra.payment.ui.beans.QuickBook;
import com.yatra.payment.ui.beans.UserProfile;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.wallet.beans.resp.GetECashRS;


@Service("userProfileService")
public class UserProfileService {
	
	private static Logger logger = Logger.getLogger(UserProfileService.class);
	
	@Autowired private UserProfileValidator userProfileValidator;
	@Autowired private UserLoginService userLoginService;
	@Autowired private QuickBookUIService quickBookUIService;
	@Autowired private WalletServiceClient walletService;
	
	public UserProfile getUserProfile(Map<String, String> requestMap, String ssoToken) {
		
		// Get email Id from request.
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		try {
			logger.info("Get User profile request received for email Id : " + emailId + " and ssoToken : " + ssoToken);
			
			// Validate request.
			String error = userProfileValidator.validateGetUserProfile(requestMap, ssoToken);
			logger.info("Get User profile request validation response for email Id : " + emailId + " and ssoToken : " + ssoToken + " : " + error);
			
			// Determine if request is valid.
			boolean isValid = StringUtils.isEmpty(error);
			logger.info("Is Get User profile request valid for email Id : " + emailId + " and ssoToken : " + ssoToken + " : " + isValid);
			
			// Return UserProfile depending upon request validation result.
			return isValid ? createUserProfile(requestMap, ssoToken) : getErrorUserProfile(PaymentUIUtil.ERROR_CODE_INVALID, error, emailId);
			
		} catch (Exception e) {
			logger.error("Exception occurred while getting UserProfile for emailId : " + emailId + " and ssoToken : " + ssoToken, e);
			return getErrorUserProfile(PaymentUIUtil.ERROR_CODE_EXCEPTION, e.getMessage(), emailId);
		}
	}
	
	public UserProfile authenticateYatraUser(Map<String, String> requestMap, HttpServletResponse response) {
		String email = requestMap.get(PaymentUIUtil.EMAIL);
		try {
			logger.info("Authenticating user with email : " + email);
			String password = requestMap.get(PaymentUIUtil.PASSWORD);
			
			// Login user.
			String ssoToken = userLoginService.login(email, password,"YATRA");
			
			// Determine if login is successful.
			boolean isLoginSuccessful = StringUtils.isNotEmpty(ssoToken);
			logger.info("Is login of user with email : " + email + " successful : " + isLoginSuccessful);
			
			// If login is successful, add ssoToken to response.
			if(isLoginSuccessful)
				userLoginService.addSSOTokenCookie(response, ssoToken);
			
			// Get UserProfile depending upon whether login was successful or not.
			return isLoginSuccessful ? getUserProfile(requestMap, ssoToken) : getAuthFailedUserProfile(email, PaymentUIUtil.AUTH_MODE_YATRA, PaymentUIUtil.ERROR_CODE_AUTH_FAILED, PaymentUIUtil.ERROR_MSG_AUTH_FAILED);
			
		} catch (Exception e) {
			logger.error("Exception occurred while authenticating yatra user with email : " + email, e);
			return getErrorUserProfile(PaymentUIUtil.ERROR_CODE_EXCEPTION, e.getMessage(), email);
		}
	}
	
	private UserProfile createUserProfile(Map<String, String> requestMap, String ssoToken) throws Exception {
		
		// Get email Id from request.
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		logger.info("Creating UserProfile for email : " + emailId + " and ssoToken : " + ssoToken);

		// Create UserProfile
		UserProfile userProfile = new UserProfile();
		userProfile.setEmailId(emailId);
		
		// Get Auth Mode. (This is the login mode - YATRA / FACEBOOK)
		String authMode = userLoginService.getAuthMode(ssoToken);
		logger.info("Auth mode for email : " + emailId + " and ssoToken : " + ssoToken + " is : " + authMode);
		userProfile.setAuthMode(authMode);
		
		// Check if user is password compromised or not. (Password compromised users are those whose passwords are stored in clear text in our DB)
		boolean isPasswordCompromised = Boolean.valueOf(userLoginService.isPasswordCompromised(ssoToken));
		logger.info("Is user with email : " + emailId + " and ssoToken : " + ssoToken + " password compromised : " + isPasswordCompromised);
		userProfile.setIsPasswordCompromised(isPasswordCompromised);
		
		// Check if user is authenticated. (Use is authenticated when his login is not very old)
		boolean isUserAuthenticated = Boolean.valueOf(userLoginService.isUserAuthenticated(ssoToken));
		logger.info("Is user with email : " + emailId + " and ssoToken : " + ssoToken + " authenticated : " + isUserAuthenticated);
		
		// Add if authentication is required (re-login, since login is old). Authentication is required only when user has either QB cards or eCash and he is not authenticated (isUserAuthenticated = false).
		addAuthenticationNeeded(requestMap, ssoToken, userProfile, isUserAuthenticated);
		
		// If user is not authenticated (his login is old), do not add QB cards and eCash. Return.
		if(!isUserAuthenticated) 
			return userProfile;
		
		// Add Quick Book Cards to User Profile.
		addQBCards(userProfile, requestMap, ssoToken);
		
		// Add eCash to User Profile.
		addECash(userProfile, requestMap);
		
		logger.info("Returning UserProfile : " + userProfile + " for user with email : " + emailId + "  and ssoToken : " + ssoToken);
		return userProfile;
	}
	
	private UserProfile getErrorUserProfile(String errorCode, String errorMessage, String emailId) {
		logger.info("Creating error UserProfile for email : " + emailId + ". Error code : " + errorCode + " and error message : " + errorMessage);
		UserProfile userProfile = new UserProfile();
		userProfile.setEmailId(emailId);
		userProfile.setErrorCode(errorCode);
		userProfile.setErrorMessage(errorMessage);
		return userProfile;
	}
	
	private void addQBCards(UserProfile userProfile, Map<String, String> requestMap, String ssoToken) throws Exception {
		boolean isPasswordCompromised = userProfile.getIsPasswordCompromised();
		if(!isPasswordCompromised) { // Password compromised users do not have saved QB cards.
			String emailId = requestMap.get(PaymentUIUtil.EMAIL);
			String merchantCode = requestMap.get(PaymentUIUtil.MERCHANT_CODE);
			String productCode = requestMap.get(PaymentUIUtil.PRODUCT_CODE);
			
			logger.info("Adding QB Cards to UserProfile for email : " + emailId + ", ssoToken : " + ssoToken + ", merchant : " + merchantCode + " and product : " + productCode);
			
			QuickBookPaymentOption qbPaymentOption;
			try {
				qbPaymentOption = quickBookUIService.getQuickBookBean(merchantCode, productCode, ssoToken, 0L, null);
			} catch (Exception e) {
				logger.error("Exception occurred while getting QuickBook Bean : ", e);
				qbPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
			}
			QuickBook quickBook = createQuickBookBean(qbPaymentOption);
			userProfile.setQuickBook(quickBook);
		}
	}
	
	private QuickBook createQuickBookBean(QuickBookPaymentOption qbPaymentOption) {
		return new QuickBook(qbPaymentOption.getQuickBookCards(), qbPaymentOption.getErrorCode(), qbPaymentOption.getErrorMesaage());
	}
	
	private void addECash(UserProfile userProfile, Map<String, String> requestMap) throws Exception {
		
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		logger.info("Adding eCash to UserProfile for email : " + emailId);
		
		GetECashRS getECashRS = walletService.getECash(requestMap);
		EWallet eWallet = createEWalletBean(getECashRS);
		userProfile.seteWallet(eWallet);
	}
	
	private EWallet createEWalletBean(GetECashRS getECashRS) {
		boolean hasECash = getECashRS.isStatus() && (getECashRS.getECashList() != null && getECashRS.getECashList().size() > 0);
		String errorMsg = hasECash ? null : getECashRS.getResponseMsg();
		String errorCode = hasECash ? null : PaymentUIUtil.ERROR_CODE_UNKNOWN;
		return new EWallet(getECashRS.getTotalEcash(), getECashRS.getECashList(), errorCode, errorMsg);	
	}
	
	private void addAuthenticationNeeded(Map<String, String> requestMap, String ssoToken, UserProfile userProfile, boolean isUserAuthenticated) {
		
		boolean authenticationNeeded = false;
		
		if(!isUserAuthenticated) {
			
			// Get emailId.
			String emailId = requestMap.get(PaymentUIUtil.EMAIL);
			
			// Check if user is password compromised.
			boolean isPasswordCompromised = userProfile.getIsPasswordCompromised();
			
			// Check if user has saved cards. Password compromised users do not have saved cards.
			boolean hasSavedCards = !isPasswordCompromised && hasSavedCards(requestMap, ssoToken, userProfile);
			
			// Check if user has eCash.
			boolean hasECash = hasECash(requestMap, userProfile);
			logger.info("User with emailId : " + emailId + " has saved Cards : " + hasSavedCards + " and eCash : " + hasECash);
			
			// Authentication is required only when user has either QB cards or eCash.
			authenticationNeeded = hasSavedCards || hasECash;
		}
		userProfile.setIsAuthenticationNeeded(authenticationNeeded);
	}
	
	private boolean hasSavedCards(Map<String, String> requestMap, String ssoToken, UserProfile userProfile) {
		
		// Get details from requestMap.
		String merchantCode = requestMap.get(PaymentUIUtil.MERCHANT_CODE);
		String productCode = requestMap.get(PaymentUIUtil.PRODUCT_CODE);
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		
		// Get QuickBookPaymentOption bean to determine if user has saved cards or not.
		QuickBookPaymentOption qbPaymentOption;
		try{
			qbPaymentOption = quickBookUIService.getQuickBookBean(merchantCode, productCode, ssoToken, 0L, null);
		} catch (Exception e) {
			logger.error("Exception occurred while getting QuickBook Bean : ", e);
			qbPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
		}
		logger.debug("QuickBookPaymentOption bean received while determining whether user with email : " + emailId + " and ssoToken : " + ssoToken + " has saved cards : " + qbPaymentOption);
		
		// Check if user has saved cards.
		boolean hasSavedCards = qbPaymentOption.hasSavedCards();
		
		// If user does not have saved cards, add error code and error message to UserProfile, so that the reason is known.
		if(!hasSavedCards) { 
			QuickBook quickBook = createQuickBookBean(qbPaymentOption);
			userProfile.setQuickBook(quickBook);
		}
		return hasSavedCards;
	}
	
	private boolean hasECash(Map<String, String> requestMap, UserProfile userProfile) {
		
		// Get details from requestMap.
		String emailId = requestMap.get(PaymentUIUtil.EMAIL);
		
		// Hit Wallet Service to get eCash.
		GetECashRS getECashRS = walletService.getECash(requestMap);
		logger.debug("GetECashRS bean received while determining whether user with email : " + emailId + " has eCash : " + getECashRS);
		
		// Check if user has eCash.
		boolean hasECash = getECashRS.isStatus() && (getECashRS.getECashList() != null && getECashRS.getECashList().size() > 0);
		
		// If user does not have eCash, add error code and error message to UserProfile, so that the reason is known.
		if(!hasECash) {
			EWallet eWallet = createEWalletBean(getECashRS);
			userProfile.seteWallet(eWallet);
		}
		return hasECash;
	}
	
	private UserProfile getAuthFailedUserProfile(String emailId, String authMode, String errorCode, String errorMsg) {
		logger.info("Creating auth fail UserProfile for email : " + emailId + ". Error code : " + errorCode + " and error message : " + errorMsg);
		UserProfile userProfile = new UserProfile();
		userProfile.setIsAuthenticationNeeded(true);
		userProfile.setEmailId(emailId);
		userProfile.setAuthMode(authMode);
		userProfile.setErrorCode(errorCode);
		userProfile.setErrorMessage(errorMsg);
		return userProfile;
	}
}
