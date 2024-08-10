package com.yatra.payment.ui.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.cards.client.ExpressCardsServiceClient;
import com.yatra.express.cards.util.ExpressCardsPropertiesConstants;
import com.yatra.express.cards.v3.beans.ResponseStatus;
import com.yatra.express.cards.v3.beans.UserCardInfo;
import com.yatra.express.cards.v3.beans.UserCardPropertiesInfo;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;

@Service("quickBookSaveCardService")
public class QuickBookSaveCardService {

	private static Logger logger = Logger.getLogger(QuickBookSaveCardService.class);

	@Autowired	private ExpressCardsServiceClient expressCardsServiceClient;
	@Autowired private TenantService tenantService;
	@Autowired private UserLoginService userLoginService;
	@Autowired private QuickBookValidator quickBookValidator;

	public String saveQuickBookCard(String ssoToken, Map<String, String> requestMap) {

		try {
			if(!PaymentUIUtil.isTokenValid(ssoToken, requestMap.get("csrfToken"))){
				QuickBookUIUtil.getExceptionJSON("Invalid Csrf Token");
			}
			// Check if user is authenticated (when login has not expired).
			boolean isAuthenticated = userLoginService.isUserAuthenticated(ssoToken);
			if(!isAuthenticated) {
				return QuickBookUIUtil.getAuthRequiredJSON();
			}

			// Validate request.
			String error = quickBookValidator.validateSaveCardRequest(ssoToken, requestMap);
			boolean isValid = StringUtils.isEmpty(error);
			if (!isValid)
				return QuickBookUIUtil.getInvalidRequestJSON(error);

			// Save card.
			Long tenantId = getTenantId(requestMap);
			UserCardInfo userCardInfo = getUserCardInfo(requestMap);
			ResponseStatus responseStatus = expressCardsServiceClient.addCard(tenantId, ssoToken, userCardInfo);
			return QuickBookUIUtil.getResponseJSON(responseStatus);

		} catch (Exception e) {
			logger.error("Exception occurred while saving card details for ssoToken : " + ssoToken + " : ", e);
			return QuickBookUIUtil.getExceptionJSON(e.getMessage());
		}
	}



	public String saveQBCardTemporarily(String ssoToken, Map<String,String> requestMap){
		try {
			/*
			// Check if user is authenticated (when login has not expired).
			boolean isAuthenticated = userLoginService.isUserAuthenticated(ssoToken);
			if(!isAuthenticated) {
				return QuickBookUIUtil.getAuthRequiredJSON();
			}
			*/
			logger.info("Not doing authentication check for saveQBCardTemporarily (passthrough flow)");

			// Validate request.
			String error = quickBookValidator.validateSaveCardRequest(ssoToken, requestMap);
			boolean isValid = StringUtils.isEmpty(error);
			if (!isValid)
				throw new PayswiftException(FailureCode.SAVE_PASSSTHROUGH_CARD_EXCEPTION.getCode(), "An Exception Occured while validating save card request " + error);

			// Save card.
			logger.debug("Saving card termporarily for sso token "+ssoToken);
			Long tenantId = getTenantId(requestMap);
			UserCardInfo userCardInfo = getUserCardInfo(requestMap);
			addTemporaryFlagInCardProperties(userCardInfo);
			ResponseStatus responseStatus = expressCardsServiceClient.addCard(tenantId, ssoToken, userCardInfo);
			JSONObject saveCardJson = new JSONObject(QuickBookUIUtil.getResponseJSON(responseStatus));
			logger.info("JSON response recieved from expressCardsClientService addCard method : "+ QuickBookUIUtil.getResponseJSON(responseStatus));
			if(saveCardJson.has(PaymentUIUtil.CARD_ID)){
				return saveCardJson.getString(PaymentUIUtil.CARD_ID);
			}

		} catch (Exception ex) {
			throw new PayswiftException(FailureCode.SAVE_PASSSTHROUGH_CARD_EXCEPTION.getCode(), "An Exception Occured while saving card for pass through booking", ex);
		}
		return null;
	}


	private void addTemporaryFlagInCardProperties(UserCardInfo userCardInfo) {
		//Just for debugging purposes , remove this after Development Done .
		logger.debug("Adding save card Temporary to card Property ");
		HashMap<String,String> cardProperties = userCardInfo.getCardProperties();
		if(cardProperties == null){
			cardProperties = new HashMap<String,String>();
		}
		cardProperties.put(PaymentUIUtil.SAVE_CARD_TEMPORARILY,"true");
		userCardInfo.setCardProperties(cardProperties);

	}


	public ResponseStatus saveBillingAddressForQuickBookCard(Long tenantId, String ssoToken, Map<String,String> parameterMap){
		ResponseStatus saveAddResponse = null;
		try{
			UserCardPropertiesInfo userCardPropertiesInfo = getUserCardPropertiesInfo(parameterMap);
			if(userCardPropertiesInfo != null) {
				saveAddResponse = expressCardsServiceClient.addCardProperties(tenantId, ssoToken, userCardPropertiesInfo);
				logger.debug("saveBillingAddressForQuickBookCard - responseStatus.getErrorCode: " + saveAddResponse.getErrorCode() + " ,responseStatus.getErrorMessage:" + saveAddResponse.getErrorMessage());
			}
		}catch (Exception e) {
			logger.error("Exception occurred while saving billing address details for ssoToken : " + ssoToken + " : ", e);
		}
		return saveAddResponse;
	}

	private Long getTenantId(Map<String, String> requestMap) {
		String merchantCode = requestMap.get("merchant");
		String productCode = requestMap.get("product");
		return tenantService.getTenantId(merchantCode, productCode);
	}

	public UserCardInfo getUserCardInfo(Map<String, String> parameterMap)  {
		try{

			//Get Details from parameterMap
			String cardType = parameterMap.get(PaymentUIUtil.CARD_TYPE);
			String cardBrand = parameterMap.get(QuickBookUIUtil.CARD_BRAND);
			String firstName = PaymentUIUtil.getFirstName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));
			String lastName = PaymentUIUtil.getLastName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));

			String cardExpiryYear = parameterMap.get(PaymentUIUtil.CARD_EXP_YEAR);
			String cardExpiryMonth = parameterMap.get(PaymentUIUtil.CARD_EXP_MONTH);
			String cardNumber = parameterMap.get(PaymentUIUtil.CARD_NO);

			//Create UserCardInfo
			if(StringUtils.equals(cardType, PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD) )
				cardType = QuickBookUIUtil.CARD_TYPE_CREDIT_CARD;

			if(StringUtils.equals(cardType, PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD) )
				cardType = QuickBookUIUtil.CARD_TYPE_DEBIT_CARD;

			String cardName = parameterMap.get(QuickBookUIUtil.CARD_NAME);
			cardName = StringUtils.isEmpty(cardName) ? (cardBrand + cardType) : cardName;

			UserCardInfo userCardInfo = new UserCardInfo();
			userCardInfo.setCardType(cardType);
			userCardInfo.setCardBrand(cardBrand);
			userCardInfo.setCardName(cardName);
			userCardInfo.setExpiryMonth(cardExpiryMonth);
			userCardInfo.setExpiryYear(cardExpiryYear);
			userCardInfo.setTitle(QuickBookUIUtil.CARD_HOLDER_TITLE);
			userCardInfo.setFirstName(firstName);
			userCardInfo.setLastName(lastName);
			userCardInfo.setUserId(MapUtils.getLong(parameterMap, PaymentUIUtil.USER_ID));

			// Determine if card is international.
			String isCardInternational = parameterMap.get(PaymentUIUtil.IS_CARD_INTERNATIONAL);

			// Set card access type - DOM /INT.
			String cardAccessType = StringUtils.equalsIgnoreCase(isCardInternational, "true") ? QuickBookUIUtil.CARD_ACCESS_TYPE_INT : QuickBookUIUtil.CARD_ACCESS_TYPE_DOM;
			userCardInfo.setCardAccessType(cardAccessType);

			// Set address details for International cards.
			if (StringUtils.equalsIgnoreCase(isCardInternational, "true")) {
				Map<String, String> billingAddressMap = getBillingAddress(parameterMap);
				userCardInfo.setCardProperties((HashMap<String, String>)billingAddressMap);
			}

			String userBookingType = parameterMap.get(PaymentUIUtil.BOOKING_TYPE);
			if(userCardInfo.getCardProperties() != null){
				userCardInfo.getCardProperties().put(ExpressCardsPropertiesConstants.BOOKING_TYPE, userBookingType);
			} else{
				HashMap<String, String> cardProperties = new HashMap<String, String>();
				cardProperties.put(ExpressCardsPropertiesConstants.BOOKING_TYPE, userBookingType);
				userCardInfo.setCardProperties(cardProperties);
			}

			//Set Card Number
			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_VISA) || StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_MASTER)){
				userCardInfo.setCardNumber1(cardNumber.substring(0, 4));
				userCardInfo.setCardNumber2(cardNumber.substring(4, 8));
				userCardInfo.setCardNumber3(cardNumber.substring(8, 12));
				userCardInfo.setCardNumber4(cardNumber.substring(12));
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_DINERS) || StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_DINNERS)){
				userCardInfo.setCardBrand(QuickBookUIUtil.CARD_BRAND_DINERS);
				userCardInfo.setCardNumber1(cardNumber.substring(0, 4));
				userCardInfo.setCardNumber2(cardNumber.substring(4, 10));
				userCardInfo.setCardNumber3(cardNumber.substring(10));
				userCardInfo.setCardNumber4("");
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_AMEX)){
				userCardInfo.setCardNumber1(cardNumber.substring(0, 4));
				userCardInfo.setCardNumber2(cardNumber.substring(4, 10));
				userCardInfo.setCardNumber3(cardNumber.substring(10));
				userCardInfo.setCardNumber4("");
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_MAESTRO)){
				userCardInfo.setCardBrand(QuickBookUIUtil.CARD_BRAND_MAESTRO);
				userCardInfo.setCardNumber1(cardNumber.substring(0, 5));
				userCardInfo.setCardNumber2(cardNumber.substring(5, 10));
				userCardInfo.setCardNumber3(cardNumber.substring(10, 15));
				userCardInfo.setCardNumber4(cardNumber.substring(15));
			}

			return userCardInfo;

		}catch (Exception e) {
			logger.error("Exception occurred while saving user's card details for Quick Book : ", e);
			throw e;
		}
	}

	private UserCardPropertiesInfo getUserCardPropertiesInfo(Map<String, String> parameterMap) {
		try {
			// Determine if card is international.
			String isCardInternational = parameterMap.get(PaymentUIUtil.IS_CARD_INTERNATIONAL);

			String cardAccessType = StringUtils.equalsIgnoreCase(isCardInternational, "true") ? QuickBookUIUtil.CARD_ACCESS_TYPE_INT : QuickBookUIUtil.CARD_ACCESS_TYPE_DOM;
			UserCardPropertiesInfo userCardPropertiesInfo = new UserCardPropertiesInfo();
			userCardPropertiesInfo.setCardAccessType(cardAccessType);

			// Get address details for International card.
			if (StringUtils.equalsIgnoreCase(isCardInternational, "true")) {
				Map<String, String> billingAddressMap = getBillingAddress(parameterMap);
				userCardPropertiesInfo.setCardProperties((HashMap<String, String>)billingAddressMap);
			}
			return userCardPropertiesInfo;

		} catch (Exception e) {
			logger.error("Exception occurred while saving user's billing address details for Quick Book : ", e);
			return null;
		}
	}

	private Map<String, String> getBillingAddress(Map<String, String> parameterMap) {
		Map<String, String> cardProperties = new HashMap<String, String>();
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.ADDRESS1, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_1));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.ADDRESS2, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_2));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.CITY, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_CITY));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.STATE, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_STATE));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.PINCODE, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_PIN));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.COUNTRY, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_COUNTRY));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.ISDCODE, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_ISD));
		PaymentUIUtil.populateStringMap(cardProperties, ExpressCardsPropertiesConstants.MOBILE_NUMBER, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_MOBILE));
		return cardProperties;
	}



	public String saveGuestUserCard(Map<String, String> requestMap) {
		try {

			// Validate request.
			String error = quickBookValidator.validateSaveGuestCardRequest(requestMap);
			boolean isValid = StringUtils.isEmpty(error);
			if(!isValid)
				return QuickBookUIUtil.getInvalidRequestJSON(error);

			// Save card.
			Long tenantId = getTenantId(requestMap);
			Long userId = MapUtils.getLong(requestMap, PaymentUIUtil.USER_ID);
			logger.debug("Saving card temporarily for guest user with id  "+ userId);

			UserCardInfo userCardInfo = getUserCardInfo(requestMap);
			addTemporaryFlagInCardProperties(userCardInfo);
			ResponseStatus responseStatus = expressCardsServiceClient.addCardForGuestUser(tenantId, userId, userCardInfo);
			JSONObject saveCardJson = new JSONObject(QuickBookUIUtil.getResponseJSON(responseStatus));
			logger.debug("Response received from QuickBook service "+saveCardJson);
			if(saveCardJson.has(PaymentUIUtil.CARD_ID)){
				return saveCardJson.getString(PaymentUIUtil.CARD_ID);
			}

		} catch (Exception e) {
			throw new PayswiftException(FailureCode.SAVE_GUEST_USER_CARD_EXCEPTION.getCode(), "An Exception Occured while saving card for guest user flow", e);
		}
		return null;
	}
}
