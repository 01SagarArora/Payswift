package com.yatra.payment.ui.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.CardTypeCache;
import com.yatra.payment.cache.service.impl.CardTypeParamMappingCentralCache;
import com.yatra.payment.ui.dao.bean.UIPayopCardTypesMapping;
import com.yatra.payment.ui.display.beans.BillingAddress;
import com.yatra.payment.ui.display.beans.CardDetail;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.CardValidations;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

public abstract class CardOptionBuilder implements PaymentOptionBuilder {
	
	private static Logger logger = Logger.getLogger(CardOptionBuilder.class);
	
	@Autowired private CardTypeParamMappingCentralCache cardTypeParamMappingCentralCache;
	@Autowired private UICacheManager<UIPayopCardTypesMapping> payopCardTypesMappingCache;
	@Autowired private CardTypeCache cardTypeCache;
	@Autowired protected StatusProviderService statusProviderService;

	protected static String COMMON_CONSTANT = "common";
	
	protected List<CardType> getEnabledCardTypes(String paymentOption, String merchantCode, String productCode) throws Exception {
		
		// This API gives list of card types enabled for a given payment option for this merchant and product.
		logger.info("Getting enabled card types for paymentOption : " + paymentOption + ", merchant : " + merchantCode + " and product : " + productCode);
		
		List<CardType> cardTypeList = new ArrayList<CardType>();
		try {
			// Get all card types for given payment option.
			List<UIPayopCardTypesMapping> payopCardTypesMappingList = payopCardTypesMappingCache.getAll(paymentOption);
			
			// If no card types are obtained for given payment option (payopCardTypesMappingList is null or empty), return an empty list.
			if(payopCardTypesMappingList == null || payopCardTypesMappingList.isEmpty()) {
				logger.info("No card types obtained for payment option : " + paymentOption + " from PayOpCardTypesMapping cache.");
				return cardTypeList;
			}
			// Iterate over all card types for given payment option.
			for (UIPayopCardTypesMapping payopCardTypeMapping : payopCardTypesMappingList) {
				
				// Get CardType code.
				String cardTypeCode = payopCardTypeMapping.getCardType();
				
				// Check if card type is enabled.
				boolean isCardTypeEnabled = PaymentUIUtil.isEnabled(payopCardTypeMapping.getStatus());
				logger.info("Is Card Type : " + cardTypeCode + " enabled : " + isCardTypeEnabled);
				
				// If this card type is enabled, create CardType bean for given merchant and product.
				if(isCardTypeEnabled) {
					// Get CardType bean for merchant and add it to list if not null. 
					CardType cardTypeForMerchant = getCardTypeForMerchant(merchantCode, productCode, cardTypeCode, paymentOption);
					if(cardTypeForMerchant != null)
						cardTypeList.add(cardTypeForMerchant);
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred while getting enabled Card Types for payment option " + paymentOption + ", merchant : " + merchantCode + " and product " + productCode, e);
		}
		return cardTypeList;
	}
	
	private CardType getCardTypeForMerchant(String merchantCode, String productCode, String cardTypeCode, String paymentOption) throws Exception {
		
		CardType cardTypeForMerchant = null;
		
		// Check if this card type is enabled for given merchant and product.
		Boolean isCardTypeEnabledForMerchant = statusProviderService.isCardTypeEnabled(merchantCode, productCode, cardTypeCode);
		logger.info("Is Card Type : " + cardTypeCode + " enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isCardTypeEnabledForMerchant);
		
		// If this card type is enabled for given merchant and product, create CardType bean.
		if(isCardTypeEnabledForMerchant != null && isCardTypeEnabledForMerchant) {
			
			// Get CardType bean from cache.
			CardType cardType = cardTypeCache.get(cardTypeCode);
			if(cardType != null) {
				
				// Build CardType bean for given merchant and product.
				cardTypeForMerchant = new CardType(cardType);
				CardDetail cardDetail = getCardDetail(paymentOption, cardTypeForMerchant);
				CardValidations cardValidations = getCardValidations(paymentOption, cardTypeForMerchant);
				cardTypeForMerchant.setCardDetail(cardDetail);
				cardTypeForMerchant.setCardValidations(cardValidations);
				cardTypeForMerchant.setStatus(PaymentUIUtil.getStatus(isCardTypeEnabledForMerchant));
				
			} else {
				logger.info("No Card Type bean obtained from CardTypeCache for cardType : " + cardTypeCode);
			}
		}
		return cardTypeForMerchant;
	}
	
	private CardDetail getCardDetail(String paymentOption, CardType cardType) throws Exception {
		CardDetail cardDetail = new CardDetail();
		cardDetail.setExpiryDateLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "expiryDateLabel"));
		cardDetail.setCvvLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cvvLabel"));
		cardDetail.setCardNoLength(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "cardNoLength"));
		cardDetail.setCvvLength(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cvvLength"));
		cardDetail.setCvvImageURL(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cvvImageURL"));
		cardDetail.setCvvImageText(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cvvImageText"));
		//cardDetail.setWithoutCVVSupported(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "withoutCVVSupported"));

		if (cardType.getCode().equalsIgnoreCase("VISA")) {
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "withoutCVVSupported"));
		}
		if (cardType.getCode().equalsIgnoreCase("MASTER")) {
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "withoutCVVSupported"));
		}
		if (cardType.getCode().equalsIgnoreCase("RUPAY")) {
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "withoutCVVSupported"));
		}
		if (cardType.getCode().equalsIgnoreCase("DINERS")) {
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "withoutCVVSupported"));
		}
		if (cardType.getCode().equalsIgnoreCase("AMEX")) {
			cardDetail.setCardNoLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "cardNoLength"));
			cardDetail.setCvvLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "cvvLength"));
			cardDetail.setCvvImageURL(getUIParamMappingValue(paymentOption, cardType.getCode(), "cvvImageURL"));
			cardDetail.setCvvImageText(getUIParamMappingValue(paymentOption, cardType.getCode(), "cvvImageText"));
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(paymentOption, cardType.getCode(), "withoutCVVSupported"));
		}
		if (cardType.getCode().equalsIgnoreCase("MAEST")) {
			cardDetail.setCardNoLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "cardNoLength"));
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(paymentOption, cardType.getCode(), "withoutCVVSupported"));
		}
		if (cardType.getCode().equalsIgnoreCase("BAJAJ")) {
			cardDetail.setCardNoLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "cardNoLength"));
			cardDetail.setWithoutCVVSupported(getUIParamMappingValue(paymentOption, cardType.getCode(), "withoutCVVSupported"));
		}
		return cardDetail;
	}
	
	private CardValidations getCardValidations(String paymentOption, CardType cardType) throws Exception {
		CardValidations cardValidations= new CardValidations();
		cardValidations.setCvvLength(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cvvLength"));
		cardValidations.setCvv(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cvv"));
		cardValidations.setExpiryDate(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "expiryDate"));
		
		cardValidations.setValidateLuhn(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "validateLuhn"));
		cardValidations.setRegex(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "regex"));
		cardValidations.setCardNoLength(getUIParamMappingValue(COMMON_CONSTANT, cardType.getCode(), "validCardNoLength"));
		
		if(cardType.getCode().equalsIgnoreCase("MAEST")){
			cardValidations.setCvv(getUIParamMappingValue(paymentOption, cardType.getCode(), "cvv"));
			cardValidations.setExpiryDate(getUIParamMappingValue(paymentOption, cardType.getCode(), "expiryDate"));
			cardValidations.setValidateLuhn(getUIParamMappingValue(paymentOption, cardType.getCode(), "validateLuhn"));
			cardValidations.setRegex(getUIParamMappingValue(paymentOption, cardType.getCode(), "regex"));
			cardValidations.setCardNoLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "validCardNoLength"));
		}
		
		if(cardType.getCode().equalsIgnoreCase("BAJAJ")){
			cardValidations.setCvv(getUIParamMappingValue(paymentOption, cardType.getCode(), "cvv"));
			cardValidations.setExpiryDate(getUIParamMappingValue(paymentOption, cardType.getCode(), "expiryDate"));
			cardValidations.setValidateLuhn(getUIParamMappingValue(paymentOption, cardType.getCode(), "validateLuhn"));
			cardValidations.setRegex(getUIParamMappingValue(paymentOption, cardType.getCode(), "regex"));
			cardValidations.setCardNoLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "validCardNoLength"));
		}
		
		if (cardType.getCode().equalsIgnoreCase("AMEX")) {
			cardValidations.setCvvLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "cvvLength"));
			cardValidations.setRegex(getUIParamMappingValue(paymentOption, cardType.getCode(), "regex"));
			cardValidations.setCardNoLength(getUIParamMappingValue(paymentOption, cardType.getCode(), "validCardNoLength"));
			cardValidations.setValidateLuhn(getUIParamMappingValue(paymentOption, cardType.getCode(), "validateLuhn"));
		}
		return cardValidations;
	}
	
	protected String getUIParamMappingValue(String paymentOption, String cardType, String paramKey) {
		String key = PaymentUIUtil.getPaymentDivKey(paymentOption, cardType, paramKey);
		return cardTypeParamMappingCentralCache.get(key) == null ? "" : cardTypeParamMappingCentralCache.get(key).getParamValue();
	}

	protected BillingAddress getUIBillingAddress() throws Exception {
		BillingAddress billingAddress = new BillingAddress();
		billingAddress.setBillingAddressLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingAddressLabel"));
		billingAddress.setBillingAddress1(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingAddress1"));
		billingAddress.setBillingAddress2(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingAddress2"));
		billingAddress.setBillingCity(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingCity"));
		billingAddress.setBillingPin(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingPin"));
		billingAddress.setBillingState(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingState"));
		billingAddress.setBillingCountry(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingCountry"));
		billingAddress.setBillingIsdCode(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingIsdCode"));
		billingAddress.setBillingMobile(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "billingMobile"));
		return billingAddress;
	}
}
