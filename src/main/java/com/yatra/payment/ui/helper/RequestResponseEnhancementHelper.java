package com.yatra.payment.ui.helper;

import java.security.MessageDigest;
import java.util.*;

import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.net.ssl.internal.www.protocol.https.BASE64Encoder;
import com.yatra.payment.client.PaymentClient;
import com.yatra.payment.gv.GiftVoucherHashUtil;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.commons.role.CallContextKeeper;
import com.yatra.platform.util.YatraUtil;

@Service
public class RequestResponseEnhancementHelper {
	
	private static Logger logger = Logger.getLogger(RequestResponseEnhancementHelper.class);
	
	@Autowired private PaymentClient paymentClient;
	@Autowired	private PropertyManager  yatraPropertyReader;
	@Autowired	private PayswiftStagesDAOImpl paymentDao;

	@Autowired private YatraPropertiesDAO yatraPropertiesDAO;



	public Map<String, String> getPaymentOptionParameters(String paymentOptionParameters) {
		Map<String, String> dataMap = new HashMap<String, String>();
		try {
			dataMap =  paymentClient.getPaymentParamsMap(paymentOptionParameters);
			//fix for netbanking ,in some cases bankCode value come into netBankDropDown parameter instead of bankCode
			if(dataMap!=null && !StringUtils.isBlank(dataMap.get("netBankDropDowm")) && StringUtils.isBlank(dataMap.get("bankCode"))){
				dataMap.put("bankCode", dataMap.get("netBankDropDowm"));
			}
			// List<String> corpTokenizedCardTypeList = Arrays.asList(yatraPropertyReader.getProperty("crp.tokenized.card.types").split("\\s*,\\s*"));
			// corpTokenizedCardTypeList.replaceAll(String::toUpperCase);

			/* checking if corporate card is tokenized and setting flag values for tokenized card payment processing   */
			if(dataMap!=null && !StringUtils.isBlank(dataMap.get(PaymentUIUtil.CARD_TYPE_KEY_FOR_CORPORATE))
					&& !StringUtils.isBlank(dataMap.get(PaymentUIUtil.CARD_ID))
					&& (StringUtils.equalsIgnoreCase(dataMap.get(PaymentUIUtil.CARD_TYPE_KEY_FOR_CORPORATE),PaymentUIUtil.CARD_TYPE_CTA) || StringUtils.equalsIgnoreCase(dataMap.get(PaymentUIUtil.CARD_TYPE_KEY_FOR_CORPORATE),PaymentUIUtil.CARD_TYPE_PCC)) ){

				/* setting default flag values*/
				dataMap.put(PaymentUIUtil.IS_CORPORATE_CARD_TOKENIZED, PaymentUIUtil.BOOLEAN_FALSE);
				dataMap.put(PaymentUIUtil.CORPORATE_CARD_TOKEN,"");
				dataMap.put(PaymentUIUtil.TOKEN_PROVIDER,"");
				dataMap.put(PaymentUIUtil.CORPORATE_TOKEN_STATUS,PaymentUIUtil.TOKEN_STATUS_INACTIVE);
				String cardid = dataMap.get(PaymentUIUtil.CARD_ID);

				Map<String,Object> tokenResultMap = paymentClient.getCorpCardTokenInfo(cardid);
				if(!MapUtils.isEmpty(tokenResultMap)){
					dataMap.put(PaymentUIUtil.IS_CORPORATE_CARD_TOKENIZED, PaymentUIUtil.BOOLEAN_TRUE);
					dataMap.put(PaymentUIUtil.CORPORATE_CARD_TOKEN,tokenResultMap.get("tokenRefId").toString());
					dataMap.put(PaymentUIUtil.TOKEN_PROVIDER,tokenResultMap.get("token_provider").toString());
					//dataMap.put(PaymentUIUtil.CVV_REQUIRED,PaymentUIUtil.BOOLEAN_FALSE);
					dataMap.put(PaymentUIUtil.CORPORATE_TOKEN_STATUS,PaymentUIUtil.TOKEN_STATUS_ACTIVE);
					dataMap.put(PaymentUIUtil.CVV_REQUIRED,"false");
				}
			}
		} catch (Exception e) {
			throw new PayswiftException(FailureCode.FAILED_TO_GET_PAYMENT_OPTION_PARAMETER.getCode(), "Failed to get PaymentOptionParameters",e);
		}
		return dataMap;
	}
	
	public Map<String, String> getFilteredPaymentOptionParameters(Map<String, String> dataMap, String payop) {
		if(!PaymentUIUtil.isCardBasedPayop(payop)) {
			dataMap.remove("cardNo");
			dataMap.remove("cno");
			dataMap.remove("ccsc");
			dataMap.remove("cexpm");
			dataMap.remove("cexpy");
		}
		return dataMap;
	}

	public Map<String, String> preparePortalMap(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {
		HashMap<String, String> pgDataMap = new HashMap<String, String>();
		LobValidationResponse validationResponse = payNowResponse.getValidationResponse();
		LobDetailResponse bookingDetails = payNowRequest.getBookingDetails();

		String amount = validationResponse.getAmountToCollect();
		if (payNowRequest.getWalletInfo() != null || payNowRequest.getVoucherRedeemedAmount() != null || payNowRequest.getBookingDetails().getIxigoMoney() != null) {
			String walletAmount = "0";
			String voucherAmount = "0";
			String ixigoMoney = "0";
			if(payNowRequest.getWalletInfo() != null) {
				walletAmount = payNowRequest.getWalletInfo().getAmountInRupees();
				pgDataMap.put("wallet_amount", walletAmount);
			} 
			
			if(payNowRequest.getVoucherRedeemedAmount() != null) {
				voucherAmount = payNowRequest.getVoucherRedeemedAmount();
				pgDataMap.put("voucher_amount", voucherAmount);
			}
			
			if(payNowRequest.getBookingDetails().getIxigoMoney() != null) {
				ixigoMoney = payNowRequest.getBookingDetails().getIxigoMoney();
				pgDataMap.put("ixigoMoney", ixigoMoney);
			}
			amount = getAmountToCollectFromPG(amount, walletAmount, voucherAmount, ixigoMoney);
		}
		pgDataMap.put(PaymentUIUtil.AGENT_CODE, payNowResponse.getAgentCode());
		pgDataMap.put(PaymentUIUtil.RURL, validationResponse.getRurl());
		pgDataMap.put(PaymentUIUtil.S2SURL, validationResponse.getS2Surl());
		pgDataMap.put(PaymentUIUtil.S2S, validationResponse.getS2S());
		pgDataMap.put(PaymentUIUtil.RURL_METHOD_TYPE, validationResponse.getRurlMethodType());
		pgDataMap.put(PaymentUIUtil.TTID, validationResponse.getTtid());
		pgDataMap.put(PaymentUIUtil.MINKASU_ID, generateMinkasuId(validationResponse.getTtid()));
		pgDataMap.put(PaymentUIUtil.AMOUNT,	amount);
		pgDataMap.put(PaymentUIUtil.MOBILE, bookingDetails.getUserMobile());
		pgDataMap.put(PaymentUIUtil.EMAIL, bookingDetails.getUserEmail());
		pgDataMap.put(PaymentUIUtil.COPORATE_ID, bookingDetails.getCorporateId());
		pgDataMap.put(PaymentUIUtil.CORPORATE_TRIP_ID, bookingDetails.getCorporateTripId());
		pgDataMap.put(PaymentUIUtil.ENTITY, bookingDetails.getEntity());
		String productCode =bookingDetails.getProductCodeForPortal();
		pgDataMap.put("product_code", productCode);
		pgDataMap.put("merchant_code", bookingDetails.getMerchantCode());
		pgDataMap.put(PaymentUIUtil.MODE, PaymentUIUtil.MODE_PURCHASE);
		pgDataMap.put(PaymentUIUtil.HASH_KEY, generateHashKey(payNowRequest,amount,payNowResponse));
		pgDataMap.put("tdate", bookingDetails.getTravelDate());
		pgDataMap.put(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL, payNowRequest.getSuperPnr());
		pgDataMap.putAll(getFilteredPaymentOptionParameters(getPaymentOptionParameters(payNowRequest.getPaymentOptionParameters()), payNowRequest.getPaymentOption()));
		if(StringUtils.equalsIgnoreCase(payNowRequest.getPaymentOption(),"upi")){
			pgDataMap.put("payerVA",payNowRequest.getPaymentOptionParamMap().get("payerVA"));
		}
		pgDataMap.put(PaymentUIUtil.TRIP_TYPE,bookingDetails.getTripType());
		pgDataMap.put(PaymentUIUtil.PAYMENT_PROCESS_TYPE, payNowRequest.getPaymentProcessType());
		pgDataMap.put(PaymentUIUtil.CVV_REQUIRED, payNowRequest.getCVVRequired());
		pgDataMap.put(PaymentUIUtil.ORIGIN, bookingDetails.getOriginCity());
		pgDataMap.put(PaymentUIUtil.DESTINATION, bookingDetails.getDestinationCity());
		pgDataMap.put(PaymentUIUtil.ORIGIN_AIRPORT, bookingDetails.getOriginAirport());
		pgDataMap.put(PaymentUIUtil.DESTINATION_AIRPORT, bookingDetails.getDestinationAirport());
		pgDataMap.put(PaymentUIUtil.ORIGIN_COUNTRY, bookingDetails.getOriginCountry());
		pgDataMap.put(PaymentUIUtil.DESTINATION_COUNTRY, bookingDetails.getDestinationCountry());
		pgDataMap.put(PaymentUIUtil.NUM_ADULT_PAX, bookingDetails.getNumAdtPax());
		pgDataMap.put(PaymentUIUtil.NUM_CHILD_PAX, bookingDetails.getNumChdPax());
		pgDataMap.put(PaymentUIUtil.NUM_INFANT_PAX, bookingDetails.getNumInfPax());
		pgDataMap.put(PaymentUIUtil.NUM_TOTAL_PAX, bookingDetails.getNumTotPax());
		pgDataMap.put(PaymentUIUtil.ADDONS, bookingDetails.getAddons());
		pgDataMap.put(PaymentUIUtil.CARRIER_NAME, bookingDetails.getCarrierName());
		pgDataMap.put(PaymentUIUtil.BOOKING_TYPE, bookingDetails.getIsDomestic());
		pgDataMap.put(PaymentUIUtil.PAX_LIST, bookingDetails.getPaxList());
		pgDataMap.put(PaymentUIUtil.TRAVEL_DATE_TIME, bookingDetails.getTravelDate());
		pgDataMap.put(PaymentUIUtil.FLIGHT_NUMBER, bookingDetails.getFlightNumber());
		pgDataMap.put(PaymentUIUtil.FLIGHT_CLASS, bookingDetails.getFlightClass());
		pgDataMap.put(PaymentUIUtil.MARKETPLACE, bookingDetails.getProductCodeForUI());
		pgDataMap.put(PaymentUIUtil.CYBERSOURCE_FINGERPRINT_ID, getCybersourceSessionId(payNowRequest));
		pgDataMap.put(PaymentUIUtil.HOTEL_NAME, bookingDetails.getHotelName());
		pgDataMap.put(PaymentUIUtil.HOTEL_COUNTRY, bookingDetails.getHotelCountry());
		pgDataMap.put(PaymentUIUtil.HOTEL_CITY, bookingDetails.getHotelCity());
		pgDataMap.put(PaymentUIUtil.HOTEL_GRADE, bookingDetails.getHotelGrade());
		pgDataMap.put(PaymentUIUtil.NUMBER_OF_NIGHT_STAY, bookingDetails.getNumberOfNightStay());
		pgDataMap.put(PaymentUIUtil.NUMBER_OF_ROOM, bookingDetails.getNumberOfRoom());
		pgDataMap.put(PaymentUIUtil.NUMBER_OF_GUEST, bookingDetails.getNumberOfGuest());
		pgDataMap.put(PaymentUIUtil.SERVICE_TYPE, bookingDetails.getServiceType());
		pgDataMap.put(PaymentUIUtil.SAPG_BOOKING_REF, bookingDetails.getBookingRef());
		pgDataMap.put(PaymentUIUtil.CARRIER_CLASS, bookingDetails.getCarrierClass());
		pgDataMap.put(PaymentUIUtil.CARRIER_NUMBER, bookingDetails.getCarrierNumber());
		pgDataMap.put(PaymentUIUtil.ACTIVITY_TYPE, bookingDetails.getActivityType());
		pgDataMap.put(PaymentUIUtil.CURRENCY, payNowRequest.getCurrency());
		if(PaymentUIUtil.checkIfInternationalProduct(productCode,yatraPropertiesDAO)){
			pgDataMap.put(PaymentUIUtil.CURRENCY_CODE, bookingDetails.getNZDCurrency());
		}
		pgDataMap.put(PaymentUIUtil.IS_ZERO_AMOUNT_TXN, validationResponse.getIsZeroAmountTxn());
		pgDataMap.put(PaymentUIUtil.AGENT_ID, bookingDetails.getAgentId());
		pgDataMap.put(PaymentUIUtil.IS_NON_SDK_FLOW, String.valueOf(payNowRequest.isNonSDKFlow()));
		pgDataMap.put(PaymentUIUtil.LDA_AMOUNT, bookingDetails.getLdaAmount());
		pgDataMap.put(PaymentUIUtil.ASI_AMOUNT, bookingDetails.getAsiAmount());
		pgDataMap.put(PaymentUIUtil.MONUMENT_CODE, bookingDetails.getMonumentCode());
		pgDataMap.put(PaymentUIUtil.SPECIAL_AMOUNT_FOR_MONUMENTS, bookingDetails.getSpecialAmountForMonuments());
		pgDataMap.put(PaymentUIUtil.PROMO_CODE, payNowRequest.getPromoCode());
		pgDataMap.put(PaymentUIUtil.ORIGIN_CITY_NAME, bookingDetails.getOriginCityName());
		pgDataMap.put(PaymentUIUtil.DESTINATION_CITY_NAME, bookingDetails.getDestinationCityName());
		pgDataMap.put(PaymentUIUtil.TOTAL_TRAVEL_TIME, bookingDetails.getTotalTravelTime());
		pgDataMap.put(PaymentUIUtil.NCEMI_FLAG, payNowRequest.isNoCostEmiPayment());
		pgDataMap.put(PaymentUIUtil.NCEMI_HK, getNoCostEMIHashKey(payNowRequest.isNoCostEmiPayment(),validationResponse.getTtid(),amount));
		//////Cash Pool Related Info
		pgDataMap.put(PaymentUIUtil.CORP_ID, payNowRequest.getCashPoolCorpId());
		pgDataMap.put(PaymentUIUtil.USER_ID, payNowRequest.getCashPoolUserId());
		addBookingTypeAndUserIdForQB(pgDataMap, bookingDetails, payNowRequest);
		addVoucherData(pgDataMap, payNowRequest);
		pgDataMap.put(PaymentUIUtil.PAY_LATER_FLOW, bookingDetails.getPayLaterFlow());
		pgDataMap.put(PaymentUIUtil.ACTUAL_BOOKING_AMOUNT, validationResponse.getActualBookingAmount());
		pgDataMap.put(PaymentUIUtil.PAH_HK, getPahHashKey(bookingDetails.getPayLaterFlow(),validationResponse.getActualBookingAmount()));
		pgDataMap.put(PaymentUIUtil.IS_QR_FLOW, isQRFlow(payNowRequest));
		pgDataMap.put(PaymentUIUtil.IS_GOOGLE_UPI_FLOW, isGoogleUPIFlow(payNowRequest));
		pgDataMap.put(PaymentUIUtil.ROUTE_TO_B2C, bookingDetails.getCrpB2cFlag());
		pgDataMap.put(PaymentUIUtil.CRP_GAYEWAY, bookingDetails.getCrpGateway());
		updateAuthFlags(payNowRequest,pgDataMap, bookingDetails);
		logger.info("bookingDetails.getCrpB2cFlag is : "+bookingDetails.getCrpB2cFlag());
		if("true".equalsIgnoreCase(payNowResponse.getMinkasuFlag())) {
			pgDataMap.put("minkasuSandboxAccessToken","d886ece01176df42e4b04b3532dcc83e");
			pgDataMap.put("minkasuAccessToken","cffbbffdffcf3b8158b7c05319f81491");
		}
		pgDataMap.put(PaymentUIUtil.RETURN_ID, validationResponse.getDbId());
		//For removing blank values
		Map<String, String> pgDataMapFinal = new HashMap<String, String>();
		pgDataMapFinal = removeParametersIfBlank(pgDataMap);
		
		return pgDataMapFinal;
		
	}

	private String generateMinkasuId(String ttid) {
		StringBuilder minkasuId = new StringBuilder(ttid);
		minkasuId.append("d");
		Long timeInMillis = Calendar.getInstance().getTimeInMillis();
		minkasuId.append(new StringBuilder(timeInMillis.toString()).reverse());
		if(minkasuId.length()>20)
			minkasuId.delete(19,minkasuId.length());
		return minkasuId.toString();
	}

	private String isQRFlow(PayNowRequest payNowRequest) {
		String isQRFlow = "false";
		if(!YatraUtil.isNullOrEmpty(payNowRequest.getSubPaymentOption())
				&& PaymentUIUtil.SUB_PAYMENT_OPTION_QR.equalsIgnoreCase(payNowRequest.getSubPaymentOption()))
			isQRFlow = "true";
		return isQRFlow;
	}

	private String isGoogleUPIFlow(PayNowRequest payNowRequest) {
		String isGoogleUPIFlow = "false";
		if(!YatraUtil.isNullOrEmpty(payNowRequest.getSubPaymentOption())
				&& PaymentUIUtil.SUB_PAYMENT_OPTION_TEZ.equalsIgnoreCase(payNowRequest.getSubPaymentOption()))
			isGoogleUPIFlow = "true";
		return isGoogleUPIFlow;
	}

	
	private void addVoucherData(HashMap<String, String> pgDataMap, PayNowRequest payNowRequest) {
		try{
			if(StringUtils.isNotEmpty(payNowRequest.getVoucherAuthCode()) && StringUtils.isNotEmpty(payNowRequest.getVoucherValidatedJSON())) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(PaymentUIUtil.PP_GV_AUTH_CODE, payNowRequest.getVoucherAuthCode());
				jsonObject.put(PaymentUIUtil.PP_GV_VOUCHERS, payNowRequest.getVoucherValidatedJSON());
				
				String voucherData = GiftVoucherHashUtil.getBase64Encoded(jsonObject.toString());
				String voucherKey = GiftVoucherHashUtil.generateHashCode(voucherData);
				pgDataMap.put(PaymentUIUtil.PP_GV_KEY, voucherKey);
				pgDataMap.put(PaymentUIUtil.PP_GV_DATA, voucherData);
				pgDataMap.put(PaymentUIUtil.PP_GV_IS_VOUCHER_APPLIED, "true");
			} 
		} catch (Exception ex) {
			logger.error("Error while genering voucher hashkey");
		}
		
	}

	private void addBookingTypeAndUserIdForQB(HashMap<String, String> pgDataMap, LobDetailResponse bookingDetails, PayNowRequest payNowRequest) {
		if("qb".equalsIgnoreCase(payNowRequest.getPaymentOption())){
			logger.debug("In RequestResponseEnhancementHelper.addBookingTypeAndUserIdForQB() for QuickBook ");
			pgDataMap.put(PaymentUIUtil.USER_ID, bookingDetails.getTravellerSSOId());
			pgDataMap.put(PaymentUIUtil.CRP_BOOKING_TYPE, bookingDetails.getBookingType());
			logger.debug("Added " + PaymentUIUtil.USER_ID + " : " + bookingDetails.getTravellerSSOId() + " and " + PaymentUIUtil.CRP_BOOKING_TYPE + " : " + bookingDetails.getBookingType());
		}
	}
	
	private String getAmountToCollectFromPG(String totalAmount,	String walletAmount, String voucherAmount, String ixigoMoney) {
		if (StringUtils.isBlank(walletAmount) && StringUtils.isBlank(voucherAmount) && StringUtils.isBlank(ixigoMoney)) {
			return totalAmount;
		}
		double amountInDouble = Double.parseDouble(totalAmount);
		double walletAmountInDouble = Double.parseDouble(walletAmount);
		double voucherAmountInDouble = Double.parseDouble(voucherAmount);
		double ixigoMoneyInDouble = Double.parseDouble(ixigoMoney);
		double amountChargeableFromPG = amountInDouble-walletAmountInDouble-voucherAmountInDouble-ixigoMoneyInDouble;
		return PaymentUIUtil.ceilTillTwoDecimalPlaces(amountChargeableFromPG).toString();
	}
	
	private static Map<String, String> removeParametersIfBlank(Map<String, String> parameterMap) {
		
		Map<String,String> requestMap = new HashMap<String,String>();
		for (Map.Entry<String, String> entry : parameterMap.entrySet())
		{
			if (StringUtils.isNotBlank(entry.getValue())){
				requestMap.put(entry.getKey(),entry.getValue());
			}
		}
		return requestMap;
	}

	private String generateHashKey(PayNowRequest payNowRequest, String amount, PayNowResponse payNowResponse) {
		String hashKey="";
		try {
			hashKey = paymentClient.generateHashKey(payNowRequest.getPaymentOptionParameters(), payNowResponse.getValidationResponse().getTtid(), amount, payNowRequest.getCVVRequired(), payNowResponse.getValidationResponse().getIsZeroAmountTxn(), payNowRequest.getBookingDetails().getIxigoMoney(),payNowRequest);
		} catch (Exception e) {
			logger.error("Error creating hash key"+e);
		}
		return hashKey;
	}

	private String getCybersourceSessionId(PayNowRequest payNowRequest) {
		String id = CallContextKeeper.getSessionId();
		if(payNowRequest.getAppCybersourceFingerprintId()!= null) {
			String appFingerprintId = payNowRequest.getAppCybersourceFingerprintId();
			String mid = YatraUtil.getPropertyManager().getProperty("cybersource.mid");
			id = appFingerprintId.replaceFirst(mid, "");
		}
		return id;
	}
	
	public Map<String, String> getFailRedirectData(PayNowResponse payNowResponse){
		Map<String, String> failDataMap = new HashMap<String, String>();
		failDataMap.put(PaymentUIUtil.RURL, payNowResponse.getValidationResponse().getRurl());
		failDataMap.put(PaymentUIUtil.IS_SUCCESSFUL, "false");
		failDataMap.put(PaymentUIUtil.RESPONSE_CODE_AJAX, PaymentUIUtil.RESPONSE_CODE_FAILURE);
		failDataMap.put(PaymentUIUtil.DESCRIPTION, PaymentUIUtil.RESPONSE_MESSAGE_TIMEOUT);
		return failDataMap;
	}
	
	private String getNoCostEMIHashKey(String ncemi, String ttid, String amount) {
			try {
				return generateHashKey(ncemi + ttid + amount + yatraPropertyReader.getProperty("hash.key.phrase"));
			} catch (Exception e) {
				logger.error("Error while computing NoCostEMI hashkey", e);
				return null;
			}
	}
	
	public String generateHashKey(String hashKeyInput) throws Exception {
		try {
			String hashKeyAlgo = yatraPropertyReader.getProperty("hash.key.algo");
			MessageDigest md5Digest = MessageDigest.getInstance(hashKeyAlgo);
			md5Digest.reset();
			md5Digest.update(hashKeyInput.getBytes());
			byte[] encrypted = md5Digest.digest();
			BASE64Encoder encoder = new BASE64Encoder();
			String base64 = encoder.encode(encrypted);
			return StringUtils.left(base64, base64.length() - 2);

		} catch (Exception e) {
			logger.error("Exception occurred while calculating the hash key : ", e);
			throw new Exception("Exception occurred while calculating the hash key : " + e);
		}
	}
	
	private String getPahHashKey(String payLaterFlow, String actualBookingAmount) {
		if(YatraUtil.isNullOrEmpty(payLaterFlow) || YatraUtil.isNullOrEmpty(actualBookingAmount))
				return null;
		else {
			try {
				return generateHashKey(payLaterFlow + actualBookingAmount + yatraPropertyReader.getProperty("hash.key.phrase"));
			} catch (Exception e) {
				logger.error("Error while computing PayAtHotel hashkey", e);
				return null;
			}
		}
	}

	private void updateAuthFlags(PayNowRequest payNowRequest ,Map pgDataMap, LobDetailResponse bookingDetails)
	{
		String dbAuthFlag = paymentDao.getYatraPropertyValueFromDB("auth.flow.blanket.enabled");
		Boolean isAuthFlowAllowed = Boolean.parseBoolean(dbAuthFlag);
		if(isAuthFlowAllowed==null || !isAuthFlowAllowed) {
			return;
		}
		String paymentOption = payNowRequest.getPaymentOption();
		paymentOption = paymentOption == null? "" : paymentOption;
		Boolean isPayOpAllowedForAuthFlow =((paymentOption.equalsIgnoreCase("cc")||paymentOption.equalsIgnoreCase("dc")||paymentOption.equalsIgnoreCase("qb")||paymentOption.equalsIgnoreCase("tqb")));
		//blanket for Auth Flow, DB based enable/disable functionality
		if(isPayOpAllowedForAuthFlow){
			pgDataMap.put(PaymentUIUtil.IS_AUTH_PREFERRED, bookingDetails.getIsAuthPreferred());
			pgDataMap.put(PaymentUIUtil.CHECK_BOOKING_STATUS_URL, bookingDetails.getCheckBookingStatusUrl());
		}
	}
	

}
