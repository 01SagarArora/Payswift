package com.yatra.payment.ui.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.b2b.cards.client.AgentExpressCardsServiceClient;
import com.yatra.express.b2b.cards.v3.beans.AgentCardInfo;
import com.yatra.express.b2b.cards.v3.beans.AgentCardLevelInfo;
import com.yatra.express.b2b.cards.v3.beans.ResponseStatus;
import com.yatra.express.cards.util.ExpressCardsPropertiesConstants;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.CorporateQuickBookUIUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;


@Service("agentQBSaveCardService")
public class AgentQBSaveCardService {


	private static Logger logger = Logger.getLogger(AgentQBSaveCardService.class);

	@Autowired	private AgentExpressCardsServiceClient agentCardsServiceClient;
	@Autowired private QuickBookValidator quickBookValidator;
	@Autowired private TenantService tenantService;

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

	public String saveB2BQBCardTemporarily(String ssoToken, Map<String,String> requestMap, String bin){
		try {
			logger.info("Not doing authentication check for saveB2BQBCardTemporarily (passthrough flow)");

			// Validate request.
			String error = quickBookValidator.validateSaveCardRequest(ssoToken, requestMap);
			boolean isValid = StringUtils.isEmpty(error);
			if (!isValid)
				throw new PayswiftException(FailureCode.SAVE_IMPORTPNR_CARD_EXCEPTION.getCode(), "An Exception Occured while validating save card request " + error);

			//error = quickBookValidator.isCardInternational(requestMap.get(PaymentUIUtil.CARD_NO));
			if(!checkBinDomestic(bin))
				throw new PayswiftException(FailureCode.SAVE_IMPORTPNR_INTERNATIONAL_CARD_EXCEPTION.getCode(), "International Card not allowed in Import PNR flow");

			// Save card.
			logger.debug("Saving card termporarily for sso token "+ssoToken);
			Long tenantId = getTenantId(requestMap);
			AgentCardInfo agentCardInfo = getAgentCardInfo(requestMap);
			addTemporaryFlagInAgentCardProperties(agentCardInfo);
			ResponseStatus responseStatus = agentCardsServiceClient.addSsoCard(ssoToken, agentCardInfo, "ADD");
			JSONObject saveCardJson = new JSONObject(getResponseJSON(responseStatus));
			return saveCardJson.toString();
		} catch (Exception ex) {
			throw new PayswiftException(FailureCode.SAVE_IMPORTPNR_CARD_EXCEPTION.getCode(), "An Exception Occured while saving card for import pnr flow booking", ex);
		}
	}

	private void addTemporaryFlagInAgentCardProperties(AgentCardInfo agentCardInfo) {
		//Just for debugging purposes , remove this after Development Done .
		logger.debug("Adding save card Temporary to card Property ");
		HashMap<String,String> cardProperties = agentCardInfo.getCardProperties();
		if(cardProperties == null){
			cardProperties = new HashMap<String,String>();
		}
		cardProperties.put(PaymentUIUtil.SAVE_CARD_TEMPORARILY,"true");
		agentCardInfo.setCardProperties(cardProperties);

	}

	public AgentCardInfo getAgentCardInfo(Map<String, String> parameterMap)  {
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

			AgentCardInfo agentCardInfo = new AgentCardInfo();
			agentCardInfo.setCardType(cardType);
			agentCardInfo.setCardBrand(cardBrand);
			agentCardInfo.setCardName(cardName);
			agentCardInfo.setExpiryMonth(cardExpiryMonth);
			agentCardInfo.setExpiryYear(cardExpiryYear);
			agentCardInfo.setTitle(QuickBookUIUtil.CARD_HOLDER_TITLE);
			agentCardInfo.setFirstName(firstName);
			agentCardInfo.setLastName(lastName);

			// Determine if card is international.
			String isCardInternational = parameterMap.get(PaymentUIUtil.IS_CARD_INTERNATIONAL);

			// Set card access type - DOM /INT.
			String cardAccessType = StringUtils.equalsIgnoreCase(isCardInternational, "true") ? QuickBookUIUtil.CARD_ACCESS_TYPE_INT : QuickBookUIUtil.CARD_ACCESS_TYPE_DOM;
			agentCardInfo.setCardAccessType(cardAccessType);

			// Set address details for International cards.
			if (StringUtils.equalsIgnoreCase(isCardInternational, "true")) {
				Map<String, String> billingAddressMap = getBillingAddress(parameterMap);
				agentCardInfo.setCardProperties((HashMap<String, String>)billingAddressMap);
			}

			String userBookingType = parameterMap.get(PaymentUIUtil.BOOKING_TYPE);
			if(agentCardInfo.getCardProperties() != null){
				agentCardInfo.getCardProperties().put(ExpressCardsPropertiesConstants.BOOKING_TYPE, userBookingType);
			} else{
				HashMap<String, String> cardProperties = new HashMap<String, String>();
				cardProperties.put(ExpressCardsPropertiesConstants.BOOKING_TYPE, userBookingType);
				agentCardInfo.setCardProperties(cardProperties);
			}

			//Set Card Number
			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_VISA) || StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_MASTER)){ 			
				agentCardInfo.setCardNumber1(cardNumber.substring(0, 4));
				agentCardInfo.setCardNumber2(cardNumber.substring(4, 8));
				agentCardInfo.setCardNumber3(cardNumber.substring(8, 12));
				agentCardInfo.setCardNumber4(cardNumber.substring(12));
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_DINERS) || StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_DINNERS)){
				agentCardInfo.setCardBrand(QuickBookUIUtil.CARD_BRAND_DINERS);
				agentCardInfo.setCardNumber1(cardNumber.substring(0, 4));
				agentCardInfo.setCardNumber2(cardNumber.substring(4, 10));
				agentCardInfo.setCardNumber3(cardNumber.substring(10));
				agentCardInfo.setCardNumber4("");
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_AMEX)){
				agentCardInfo.setCardNumber1(cardNumber.substring(0, 4));
				agentCardInfo.setCardNumber2(cardNumber.substring(4, 10));
				agentCardInfo.setCardNumber3(cardNumber.substring(10));
				agentCardInfo.setCardNumber4("");
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_MAESTRO)){
				agentCardInfo.setCardBrand(QuickBookUIUtil.CARD_BRAND_MAESTRO);
				agentCardInfo.setCardNumber1(cardNumber.substring(0, 5));
				agentCardInfo.setCardNumber2(cardNumber.substring(5, 10));
				agentCardInfo.setCardNumber3(cardNumber.substring(10, 15));
				agentCardInfo.setCardNumber4(cardNumber.substring(15));
			}
			AgentCardLevelInfo levelInfo = new AgentCardLevelInfo();
			levelInfo.setSavedAtAgentLevel(true);

			agentCardInfo.setProductGroup(CorporateQuickBookUIUtil.getListFromJsonArray("[\"ALL\"]"));
			agentCardInfo.setLevelGroup(levelInfo);

			return agentCardInfo;

		}catch (Exception e) {
			logger.error("Exception occurred while saving agent card details for Quick Book : ", e);
			throw e;
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

	private Long getTenantId(Map<String, String> requestMap) {
		String merchantCode = requestMap.get("merchant");
		String productCode = requestMap.get("product");
		return tenantService.getTenantId(merchantCode, productCode);
	}

	public static String getResponseJSON(ResponseStatus responseStatus) {
		if(responseStatus == null)
			return getNullResponseJSON();

		String status = StringUtils.equals(responseStatus.getStatus(), STATUS_SUCCESS) ? STATUS_SUCCESS : STATUS_FAILURE;
		String errorCode = responseStatus.getErrorCode();
		String errorMsg = responseStatus.getErrorMessage();
		String cardId = responseStatus.getCardId();
		long agentId = responseStatus.getAgentId();
		return getResponseJSON(status, errorCode, errorMsg,cardId,agentId);
	}

	public static String getResponseJSON(String status, String errorCode, String errorMsg, String cardId, long agentId) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("status", status);
		jsonMap.put("error_code", errorCode);
		jsonMap.put("error_msg", errorMsg);
		jsonMap.put("cardId", cardId);
		jsonMap.put("agentId", String.valueOf(agentId));
		return new JSONObject(jsonMap).toString();
	}

	public static String getAuthRequiredJSON() {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_AUTH_REQD, ERROR_MSG_AUTH_REQD,null,0l);
	}

	public static String getNullResponseJSON() {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_NULL_RESPONSE, ERROR_MSG_NULL_RESPONSE,null,0l);
	}

	public static String getExceptionJSON(String exceptionMsg) {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_EXCEPTION, exceptionMsg,null,0l);
	}

	public static String getInvalidRequestJSON(String invalidErrorMsg) {
		return getResponseJSON(STATUS_FAILURE, ERROR_CODE_INVALID, invalidErrorMsg,null,0l);
	}

	private boolean checkBinDomestic(String cardBin) {
		try {
			String url = "http://payment.service/checkout/pay/isBinDomestic?bin=" + cardBin;
			String response = PaymentUIUtil.sendGetRequest(url);
			logger.info("response for isBinDomestic : " + response);
			JSONObject resJson = new JSONObject(response);
			if("Y".equalsIgnoreCase(resJson.optString("isDomestic", "N")))
				return true;
		} catch (Exception e) {
			logger.error("exception while retrieving bin nationality, hence returning false : " + e.getMessage());
		}
		return false;
	}

}
