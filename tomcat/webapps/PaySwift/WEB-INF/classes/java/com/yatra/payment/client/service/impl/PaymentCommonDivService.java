package com.yatra.payment.client.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.beans.CardHolderAddress;
import com.yatra.payment.client.beans.CardHolderDetails;
import com.yatra.payment.client.enums.PaymentOption;
import com.yatra.payment.client.service.HashKeyService;
import com.yatra.payment.client.utils.PaymentClientUtil;
import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.platform.commons.property.PropertyManager;

@Service
public  class PaymentCommonDivService implements PaymentDivService {

	private Logger logger = Logger.getLogger(PaymentCommonDivService.class);
	@Autowired private HashKeyService hashKeyService;
	@Autowired	private PropertyManager  yatraPropertyReader;

	/**
	 * To return payment option details. These details are displayed on confirmation page.
	 * 
	 * @param paymentParamMap
	 * @return
	 * @throws Exception
	 */
	public String getPaymentOptionJson(Map<String, String> paymentParamMap) throws Exception {
		
		if(paymentParamMap == null || paymentParamMap.isEmpty()) {
			logger.error("Payment Option Name cannot be fetched. paymentParamMap is either empty or null");
			throw new Exception("Payment Option Name cannot be fetched. paymentParamMap is either empty or null");
		}

		// Get Payment Option name and code.
		String paymentOption = paymentParamMap.get(PaymentConstants.PAYMENT_OPTION_PARAM);
		String paymentOptionCode = getPaymentOptionCode(paymentOption);
		String paymentOptionName = getPaymentOptionName(paymentOption);
		
		// Get Previous Payment Option name and code (Only in case of complete payment through wallet i.e. payop = ew).
		String previousPaymentOption = paymentParamMap.get(PaymentConstants.PREVIOUS_PAYMENT_OPTION_PARAM);
		String previousPaymentOptionCode = getPaymentOptionCode(previousPaymentOption);
		String previousPaymentOptionName = getPaymentOptionName(previousPaymentOption);
		
		if(StringUtils.isEmpty(paymentOptionCode) || StringUtils.isEmpty(paymentOptionName)) 
			logger.error("Payment Option : " + paymentOption + " is not configured. No mapping found. PayOpCode : " + paymentOptionCode + " and PayOpName : " + paymentOptionName);
		
		return createPaymentOptionJSON(paymentOptionCode, paymentOptionName, previousPaymentOptionCode, previousPaymentOptionName);
	}
	
	private String getPaymentOptionName(String payOp) {
		String paymentOptionName = "";
		PaymentOption paymentOption = getPaymentOption(payOp);
		if(paymentOption == null)
			return paymentOptionName;
		
		switch(paymentOption) {
			
			case CC : paymentOptionName = PaymentConstants.PAY_OP_CC_NAME;
					  break;
					  
			case DC : paymentOptionName = PaymentConstants.PAY_OP_DC_NAME;
			          break;
			          
			case EMI : paymentOptionName = PaymentConstants.PAY_OP_EMI_NAME;
			           break;
			           
			case QB : paymentOptionName = PaymentConstants.PAY_OP_QB_NAME;
					  break;
					  
			case NB : paymentOptionName = PaymentConstants.PAY_OP_NB_NAME;
					  break;
						   
			case ATM : paymentOptionName = PaymentConstants.PAY_OP_ATM_NAME;
					   break;
					   
			case ITZ : paymentOptionName = PaymentConstants.PAY_OP_ITZ_NAME;
					   break;
					   
			case MW : paymentOptionName = PaymentConstants.PAY_OP_MW_NAME;
					  break;
			  
			case EC : paymentOptionName = PaymentConstants.PAY_OP_EC_NAME;
					  break;
					  
			case REWARDS : paymentOptionName = PaymentConstants.PAY_OP_REWARDS_NAME;
						   break;
						   
			case EW : paymentOptionName = PaymentConstants.PAY_OP_EW_NAME;
					  break;

			case CP : paymentOptionName = PaymentConstants.PAY_OP_CP_NAME;
					  break;

			case CORPCARD : paymentOptionName = PaymentConstants.PAY_OP_CORPCARD_NAME;
	  		          break;

		}
		return paymentOptionName;
	}
	
	private String getPaymentOptionCode(String payOp) {
		String paymentOptionCode = "";
		PaymentOption paymentOption = getPaymentOption(payOp);
		if(paymentOption == null)
			return paymentOptionCode;
		
		
		switch(paymentOption) {
		
			case CC : paymentOptionCode = PaymentConstants.PAY_OP_CC_CODE;
					  break;
					  
			case DC : paymentOptionCode = PaymentConstants.PAY_OP_DC_CODE;
			          break;
			          
			case EMI : paymentOptionCode = PaymentConstants.PAY_OP_EMI_CODE;
			           break;
			           
			case QB : paymentOptionCode = PaymentConstants.PAY_OP_QB_CODE;
					  break;
					  
			case NB : paymentOptionCode = PaymentConstants.PAY_OP_NB_CODE;
					  break;
						   
			case ATM : paymentOptionCode = PaymentConstants.PAY_OP_ATM_CODE;
					   break;
					   
			case ITZ : paymentOptionCode = PaymentConstants.PAY_OP_ITZ_CODE;
					   break;
					   
			case PAYLTR : paymentOptionCode = PaymentConstants.PAY_OP_PAY_LATER;
			   			break;
					   
			case MW : paymentOptionCode = PaymentConstants.PAY_OP_MW_CODE;
					  break;
			  
			case EC : paymentOptionCode = PaymentConstants.PAY_OP_EC_CODE;
					  break;
					  
			case REWARDS : paymentOptionCode = PaymentConstants.PAY_OP_REWARDS_CODE;
						   break;
						   
			case EW : paymentOptionCode = PaymentConstants.PAY_OP_EW_CODE;
					  break;
			
			case CP : paymentOptionCode = PaymentConstants.PAY_OP_CP_CODE;
			  break;

			case CORPCARD : paymentOptionCode = PaymentConstants.PAY_OP_CORPCARD_CODE;
	          break;
				
		}
		return paymentOptionCode;
	}
	
	private PaymentOption getPaymentOption(String paymentOption) {
		try {
			if(StringUtils.isEmpty(paymentOption))
				return null;
			return PaymentOption.valueOf(paymentOption.toUpperCase());
		} catch (Exception e) {
			logger.error("Exception occurred while determining paymentOption ENUM for payOp : " + paymentOption);
			return null;
		}
	}
	
	/**
	 * To generate payment parameters map from payment parameters string object.
	 * 
	 * @param paymentParams
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPaymentParamsMap(String paymentParams) throws Exception {
		try {
			// Get separator to separate payment params. Example : paymentOption=cc | cexpy=2000 | cexpm=08 | ctype=VISA
			String separator = yatraPropertyReader.getProperty("payment.params.separator");

			// Create map and return
			return PaymentClientUtil.convertStringToMap(paymentParams, separator);
			
		} catch (Exception e) {
			logger.error("Exception occurred while retrieving paymentParamsMap : " + e);
			throw new Exception("Exception occurred while retrieving paymentParamsMap : " + e);
		}
	}

	/**
	 * To generate promocode parameters map from promocode parameters string object.
	 * 
	 * @param promoCodePaymentParams
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPromoCodeParamsMap(String promoCodePaymentParams) throws Exception {
		try {
			// Get separator to separate promo code params.
			String separator = yatraPropertyReader.getProperty("promo.code.separator");

			// Create map and return
			Map<String, String> paramsMap = PaymentClientUtil.convertStringToMap(promoCodePaymentParams, separator);
			String carNumber = paramsMap.get("ecn");
			if(StringUtils.isNotEmpty(carNumber)) {
				carNumber = hashKeyService.generateHashKey(carNumber);
				paramsMap.put("ecn", carNumber);
			}
			return paramsMap;
			
		} catch (Exception e) {
			logger.error("Exception occurred while retrieving promoCodeParamsMap : " + e);
			throw new Exception("Exception occurred while retrieving promoCodeParamsMap : " + e);
		}
	}
	
	/**
	 * To fetch card details for international payments.
	 * 
	 * @param paymentParams
	 * @return
	 * @throws Exception
	 */
	public CardHolderDetails getCardHolderDetails(String paymentParams) throws Exception {
		try {
			
			// Throw exception is paymentParams is empty or null.
			if (StringUtils.isEmpty(paymentParams)) {
				logger.error("Card Holder's details cannot be fetched. paymentParams is null or empty : " + paymentParams);
				throw new Exception("Card Holder's details cannot be fetched. paymentParams is null or empty : " + paymentParams);
			}

			// Get Payment Option. Return card holder's details only when payment option is CC or DC
			Map<String, String> paymentParamsMap = getPaymentParamsMap(paymentParams);
			PaymentOption paymentOption = PaymentClientUtil.getPaymentOption(paymentParamsMap);
			if (paymentOption == PaymentOption.CC || paymentOption == PaymentOption.DC) {
				
				// Get Card Holder's details from parameterMap.
				String cardHolderName = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_NAME_PARAM);
				String cardNumber = paymentParamsMap.get(PaymentConstants.CARD_NUMBER_PARAM);
				String cardExpiryYear = paymentParamsMap.get(PaymentConstants.CARD_EXPIRY_YEAR_PARAM);
				String cardExpiryMonth = paymentParamsMap.get(PaymentConstants.CARD_EXPIRY_MONTH_PARAM);
				String cardType = paymentParamsMap.get(PaymentConstants.CARD_TYPE_PARAM);
				String cardCVV = paymentParamsMap.get(PaymentConstants.CARD_CVV_PARAM);
				String isCardInternationalParam = paymentParamsMap.get(PaymentConstants.IS_CARD_INTERNATIONAL_PARAM);
				boolean isCardInternational = StringUtils.equals(isCardInternationalParam, "true");
				
				// Create CardHolderAddress Bean from paymentParamsMap.
				CardHolderAddress cardHolderAddress = createCardHolderAddressBean(paymentParamsMap);
				
				// Create CardHolderDetails bean and return.
				return new CardHolderDetails(cardHolderName, cardNumber, cardExpiryYear, cardExpiryMonth, cardType, cardCVV, cardHolderAddress, isCardInternational);
			}
			
			// Return null if paymentOption is not CC or DC.
			return null;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching cardHolder's details : ", e);
			throw new Exception("Exception occurred while fetching cardHolder's details : " + e);
		}
	}

	private CardHolderAddress createCardHolderAddressBean(Map<String, String> paymentParamsMap) {
		String billingAddress = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_ADDRESS_PARAM);
		String billingCity = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_CITY_PARAM);
		String billingState = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_STATE_PARAM);
		String billingCountry = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_COUNTRY_PARAM);
		String billingPin = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_PIN_PARAM);
		String billingISDCode = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_ISD_CODE_PARAM);
		String billingMobile = paymentParamsMap.get(PaymentConstants.CARD_HOLDER_BILLING_MOBILE_PARAM);
		return new CardHolderAddress(billingAddress, billingCity, billingState, billingCountry, billingPin, billingISDCode, billingMobile);
	}
	
	private String createPaymentOptionJSON(String paymentOptionCode, String paymentOptionName, String previousPaymentOptionCode, String previousPaymentOptionName) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("payop_code", paymentOptionCode);
		jsonMap.put("payop_name", paymentOptionName);
		jsonMap.put("previous_payop_code", previousPaymentOptionCode);
		jsonMap.put("previous_payop_name", previousPaymentOptionName);
		return new JSONObject(jsonMap).toString();
	}

}
