package com.yatra.payment.ui.service;

import static com.yatra.payment.ui.enums.FailureCode.FETCH_CORPORATE_CARDS_EXCEPTION;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_CORPORATE_CARDS_FAILURE_STATUS;
import static java.util.Arrays.asList;

import java.util.*;

import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import net.sf.json.JSONArray;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.cards.util.ExpressCardsPropertiesConstants;
import com.yatra.express.crp.cards.client.B2BExpressCardsServiceClient;
import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.express.crp.cards.v3.beans.CorporateCardsDisplayResponse;
import com.yatra.express.crp.cards.v3.beans.ResponseStatus;
import com.yatra.payment.ui.beans.CorporateLoginResponseBean;
import com.yatra.payment.ui.util.CorporateQuickBookUIUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.payment.ui.util.SSOAndUPSCode;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

@Service("corporateCardsUIService")
public class CorporateCardsUIService {
	
	static String noCardsJson = "{\"noCardMessage\": \"There are no cards saved by User\"}";

	@Autowired private UserLoginService userLoginService;
	@Autowired private QuickBookValidator quickBookValidator;

	private static Logger logger = Logger.getLogger(CorporateCardsUIService.class);
	private static List<String> RELOGIN_ERROR_CODES = asList("103", "112");

	@Autowired private B2BExpressCardsServiceClient b2BExpressCardsServiceClient;
	private Map<String,String> ssoTokenStaleMap = new HashMap<>();;

	public String getCorporateCardsJson(String ssoToken){
		List<CorporateCardInfo> cardList =  getCorporateCardList(ssoToken);
		JSONArray cardJsonArray = new JSONArray();
		ObjectMapper mapper = new ObjectMapper();
		if(!YatraUtil.isNullOrEmpty(cardList)) {
			try{
				for (CorporateCardInfo card : cardList) {
					String cardJsonString = mapper.writeValueAsString(card);
					cardJsonArray.add(cardJsonString);
				}

			} catch(Exception e){
				logger.error("Error while preparing corporate cards json " +e );
			}
		}
		else{
			return noCardsJson;
		}

		return cardJsonArray.toString();
	}

	public List<CorporateCardInfo> getCorporateCardList(String ssoToken) {
		CorporateCardsDisplayResponse corporateCardsResponse = null;
		try {
			corporateCardsResponse = getCorporateCardsResponse(ssoToken);
			return getCorpCards(corporateCardsResponse);
		} catch (PayswiftException ex) {
			logger.error("Exception in get corp cards", ex);
			return null;
		}
	}

        public CorporateCardsDisplayResponse getCorporateCardsResponseLevelWise(String ssoToken, HashMap<String, String> cardAccessParams) {
		try {
			logger.info("Getting corporate cards response from b2BExpressCardsServiceClient ");
			return b2BExpressCardsServiceClient.getSsoCardsForDisplay(ssoToken, cardAccessParams);
		} catch (Exception e) {
			throw new PayswiftException(FETCH_CORPORATE_CARDS_EXCEPTION.getCode(), "Error occurred while getting corporate cards.", e);
		}
	}
        
	public CorporateCardsDisplayResponse getCorporateCardsResponse(String ssoToken) {
		try {
			logger.info("Getting corporate cards response from b2BExpressCardsServiceClient ");
			return b2BExpressCardsServiceClient.getSsoCardsForDisplay(ssoToken, null);
		} catch (Exception e) {
			throw new PayswiftException(FETCH_CORPORATE_CARDS_EXCEPTION.getCode(), "Error occurred while getting corporate cards.", e);
		}
	}

	public boolean checkAndUpdateSsoTokenStaleInfo(String ssoToken, CorporateCardsDisplayResponse corporateCardsResponse) {
		logger.info("Checking sso token stale info from corporate cards response ");
		String errorCode = corporateCardsResponse.getResponse().getErrorCode();
		logger.info("error code returned from corporate cards response" + errorCode);
		if(RELOGIN_ERROR_CODES.contains(errorCode)) {
			logger.info("stale sso token found:" + ssoToken);
			ssoTokenStaleMap.put(ssoToken, "true");
			return true;
		}
		ssoTokenStaleMap.put(ssoToken, "false");
		return false;
	}

	public String getSsoTokenStaleInfo(String ssoToken){
		if(ssoTokenStaleMap.containsKey(ssoToken)) {
			return ssoTokenStaleMap.get(ssoToken);
		}
		return "false";
	}

	public List<CorporateCardInfo> getCorpCards(CorporateCardsDisplayResponse response) {
		ResponseStatus responseStatus = response.getResponse();
		if(!responseStatus.getStatus().equalsIgnoreCase("SUCCESS")) {
			throw new PayswiftException(FETCH_CORPORATE_CARDS_FAILURE_STATUS.getCode(), "Response code : " + responseStatus.getStatus() + "Response message : " + responseStatus.getErrorMessage());
		}
		return response.getCorporateCards();
	}

	public List<CorporateCardInfo> getCorpCardsStatusHandling(CorporateCardsDisplayResponse response) {
		ResponseStatus responseStatus = response.getResponse();
		if(!responseStatus.getStatus().equalsIgnoreCase("SUCCESS")) {
			return new ArrayList<>();
		}
		return response.getCorporateCards();
	}

	public CorporateLoginResponseBean loginCorporateUser(String emailId, String password) {
		String tokenId ;
		String status="FAIL"; ;
		String message = "Please re-check the username/password combination . " ;
		String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/userType/CORPORATE/account/loginSession";
		JSONObject userLoginRequest = new JSONObject();
		try {
			userLoginRequest.put("emailId", emailId);
			userLoginRequest.put("password", password);
			String userTypeResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(userTypeResponse);
			tokenId = responseJsonObject.optString("ssoToken");
			
			if(SSOAndUPSCode.SUCCESSFULLY_LOGGED_IN.equals(responseJsonObject.optString("code"))){
				tokenId = responseJsonObject.optString("ssoToken");
				status="SUCCESS";
				message = "";
				logger.debug("Response received from userLogin service . tokenId : "+ responseJsonObject.optString("ssoToken"));
			}
			else{
				tokenId = null;
				status="FAIL";
				message = "Please re-check the username/password combination . " ;
				logger.debug("Null response received from from SSO Login Service ");
			}
		} catch (Exception e) {
			tokenId = null;
		}
		return new CorporateLoginResponseBean(status,tokenId,message);
	}

	public String saveCorporateQuickBookCard(String ssoToken, Map<String, String> requestMap) {
		if(!PaymentUIUtil.isTokenValid(ssoToken, requestMap.get("csrfToken"))){
			return CorporateQuickBookUIUtil.getInvalidCSRFTokenJSON(); 
		}
		if(!isUserAuthorized(ssoToken))
			return CorporateQuickBookUIUtil.getNotAuthorizedJSON();
		CorporateCardInfo cardInfo = new CorporateCardInfo();
		try {
			cardInfo = getCorporateCardInfo(requestMap);
			String target = CorporateQuickBookUIUtil.getAddCardTarget(requestMap.get(QuickBookUIUtil.TARGET));
			ResponseStatus response = b2BExpressCardsServiceClient.addSsoCard(ssoToken, cardInfo, target);
			return CorporateQuickBookUIUtil.getResponseJSON(response);
		} catch (Exception e) {
			logger.error("Exception occurred while saving card details for ssoToken : " + ssoToken + " : ", e);
			return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
		}
	}

	public CorporateCardInfo getCorporateCardInfo(Map<String, String> parameterMap)  {
		try{

			//Get Details from parameterMap
			String cardBrand = parameterMap.get(QuickBookUIUtil.CARD_BRAND);
			String cardType = parameterMap.get(PaymentUIUtil.CARD_TYPE);			
			String firstName = PaymentUIUtil.getFirstName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));
			String lastName = PaymentUIUtil.getLastName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));

			String cardExpiryYear = parameterMap.get(PaymentUIUtil.CARD_EXP_YEAR);
			String cardExpiryMonth = parameterMap.get(PaymentUIUtil.CARD_EXP_MONTH);
			String cardNumber = parameterMap.get(PaymentUIUtil.CARD_NO);
			String productGroupArray = parameterMap.get(QuickBookUIUtil.PRODUCT_GROUP);
			String levelGroupJson = parameterMap.get(QuickBookUIUtil.LEVEL_GROUP);
			String cardId = parameterMap.get(QuickBookUIUtil.CARD_ID);
			String cardName = parameterMap.get(QuickBookUIUtil.CARD_NAME);
			
			if("true".equalsIgnoreCase(String.valueOf(parameterMap.get("saveAsCorpCard")))) {
				cardType = getCorporateCardType(cardBrand);
				productGroupArray = "[\"ALL\"]";
				levelGroupJson = "{\"entityGroup\":[],\"entity\":[],\"user\":[" + parameterMap.get("usId") + "]}";
			}
			cardName = StringUtils.isEmpty(cardName) ? (cardBrand + cardType) : cardName;

			CorporateCardInfo cardInfo = new CorporateCardInfo();
			cardInfo.setCardType(cardType);
			cardInfo.setCardBrand(cardBrand);
			cardInfo.setCardName(cardName);
			cardInfo.setExpiryMonth(cardExpiryMonth);
			cardInfo.setExpiryYear(cardExpiryYear);
			cardInfo.setTitle(QuickBookUIUtil.CARD_HOLDER_TITLE);
			cardInfo.setFirstName(firstName);
			cardInfo.setLastName(lastName);
			cardInfo.setCorporateId(MapUtils.getLong(parameterMap, PaymentUIUtil.USER_ID));
			cardInfo.setCardId(cardId);

			// Determine if card is international.
			String isCardInternational = parameterMap.get(PaymentUIUtil.IS_CARD_INTERNATIONAL);

			// Set card access type - DOM /INT.
			String cardAccessType = StringUtils.equalsIgnoreCase(isCardInternational, "true") ? QuickBookUIUtil.CARD_ACCESS_TYPE_INT : QuickBookUIUtil.CARD_ACCESS_TYPE_DOM;
			cardInfo.setCardAccessType(cardAccessType);

			// Set address details for International cards.
			if (StringUtils.equalsIgnoreCase(isCardInternational, "true")) {
				Map<String, String> billingAddressMap = getBillingAddress(parameterMap);
				cardInfo.setCardProperties((HashMap<String, String>)billingAddressMap);
			}

			//Set Card Number
			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_VISA) || StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_MASTER)){
				cardInfo.setCardNumber1(cardNumber.substring(0, 4));
				cardInfo.setCardNumber2(cardNumber.substring(4, 8));
				cardInfo.setCardNumber3(cardNumber.substring(8, 12));
				cardInfo.setCardNumber4(cardNumber.substring(12));
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_DINERS) || StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_DINNERS)){
				cardInfo.setCardBrand(QuickBookUIUtil.CARD_BRAND_DINERS);
				cardInfo.setCardNumber1(cardNumber.substring(0, 4));
				cardInfo.setCardNumber2(cardNumber.substring(4, 10));
				cardInfo.setCardNumber3(cardNumber.substring(10));
				cardInfo.setCardNumber4("");
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_AMEX)){
				cardInfo.setCardNumber1(cardNumber.substring(0, 4));
				cardInfo.setCardNumber2(cardNumber.substring(4, 10));
				cardInfo.setCardNumber3(cardNumber.substring(10));
				cardInfo.setCardNumber4("");
			}

			if(StringUtils.equals(cardBrand, PaymentUIUtil.CARD_TYPE_MAESTRO)){
				cardInfo.setCardBrand(QuickBookUIUtil.CARD_BRAND_MAESTRO);
				cardInfo.setCardNumber1(cardNumber.substring(0, 5));
				cardInfo.setCardNumber2(cardNumber.substring(5, 10));
				cardInfo.setCardNumber3(cardNumber.substring(10, 15));
				cardInfo.setCardNumber4(cardNumber.substring(15));
			}
			
			cardInfo.setProductGroup(CorporateQuickBookUIUtil.getListFromJsonArray(productGroupArray));
			cardInfo.setLevelGroup(CorporateQuickBookUIUtil.getCorpCardLevelInfoBean(levelGroupJson, parameterMap.get(CorporateQuickBookUIUtil.SAVED_CARD_AT_CORP_LEVEL)));
			
			return cardInfo;

		}catch (Exception e) {
			logger.error("Exception occurred while saving user's card details for Quick Book : ", e);
			throw e;
		}
	}

	private String getCorporateCardType(String cardBrand) {
		if("MASTER".equalsIgnoreCase(cardBrand) || "VISA".equalsIgnoreCase(cardBrand))
			return "CTA";
		else if("AMEX".equalsIgnoreCase(cardBrand))
			return "BTA";
		else return null;
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

	public String deleteQuickBookCard(String ssoToken, Map<String, String> requestMap) {
		try {
			// Check if user is authenticated (when login has not expired).
			if(!PaymentUIUtil.isTokenValid(ssoToken, requestMap.get("csrfToken"))){
				return QuickBookUIUtil.getExceptionJSON("Invalid Csrf Token");
			}
			boolean isAuthenticated = isUserAuthorized(ssoToken);
			if(!isAuthenticated) {
				return CorporateQuickBookUIUtil.getNotAuthorizedJSON();
			}
			// Validate request.
			String error = quickBookValidator.validateDeleteCardRequest(ssoToken, requestMap);
			boolean isValid = StringUtils.isEmpty(error);
			if (!isValid)
				return CorporateQuickBookUIUtil.getInvalidRequestJSON(error);

			// Call Express Checkout to delete card.
			String cardId = requestMap.get("cardid");
			ResponseStatus responseStatus = b2BExpressCardsServiceClient.removeCard(ssoToken, cardId);
			return CorporateQuickBookUIUtil.getResponseJSON(responseStatus);

		} catch (Exception e) {
			logger.error("Exception occurred while deleting card for ssoToken : " + ssoToken, e);
			return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
		}
	}


	public boolean isUserAuthorized(String ssoToken) {
		logger.info("SSO TOKEN FETCHED : " + ssoToken);
		if(ssoToken == null)
			return false;
		try{
			String response = getUserProfileDetails(ssoToken);

			if(!YatraUtil.isNullOrEmpty(response)) {
				JSONObject resultJSON = new JSONObject(response);
				if(resultJSON.has("userDetail") 
						&& resultJSON.getJSONObject("userDetail").has("ug") 
						&& ("ADMIN".equalsIgnoreCase(resultJSON.getJSONObject("userDetail").getString("ug"))
								|| "AGENT".equalsIgnoreCase(resultJSON.getJSONObject("userDetail").getString("ug"))))
					return true;
			}
		}
		catch(Exception ex){
			logger.error(ex);
		}
		return false;
	}

	private String getUserProfileDetails(String ssoToken) {
		String response = null;
		try {
			String url = YatraUtil.getPropertyManager().getProperty("crp.api.url");
			HttpEndPoint endPoint = new HttpEndPoint(url,"POST");

			logger.debug("Hitting B2B service for userProfile Details with sso token  " + ssoToken );
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			HttpService service = new HttpService();
			JSONObject reqJson = new JSONObject();
			try {
				reqJson.accumulate("method", "getUserProfileDetail");
				reqJson.accumulate("ssoToken", ssoToken);
				reqJson.accumulate("application", "web");
			} catch (JSONException e) {
				logger.debug("Exception occurred in creating JSON for request ",e);
			}
			String postData = reqJson.toString();
			logger.info("Request sent to url: " + endPoint + " and params: " + postData);
			response = service.invoke(endPoint ,null , headers , postData ,null,null,true);
			logger.debug("Response received from B2B getUserProfileDetails service " + response );

		}
		catch (Exception e) {
			logger.error(e);
		}
		return response;
	}
}
