package com.yatra.payment.ui.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIMerchantPayopMapping;
import com.yatra.payment.ui.dao.bean.UIProductBanksMapping;
import com.yatra.payment.ui.dao.bean.UIProductCardTypeMapping;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("statusProviderService")
public class StatusProviderService {

	private static Logger logger = Logger.getLogger(StatusProviderService.class);
	
	@Autowired private UICacheManager<UIMerchantPayopMapping> merchantPayOpCache;
	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired private UICacheManager<UIProductCardTypeMapping> productCardTypeMappingCache;
	@Autowired private UICacheManager<UIProductBanksMapping> productBanksCache;

	public Boolean isPaymentOptionEnabled(String paymentOptionCode) {
		
		Boolean isPayOpEnabled = null;
		try {
			// Get list of all payment options.
			List<PaymentOption> paymentOptionList = paymentOptionCache.getAll();
			
			// Iterate over the list and determine if payment option with given code is enabled.
			if(paymentOptionList != null) {
				for (PaymentOption paymentOption : paymentOptionList) {
					if (paymentOption != null && StringUtils.equalsIgnoreCase(paymentOption.getCode(), paymentOptionCode)) {
						isPayOpEnabled = PaymentUIUtil.isEnabled(paymentOption.getStatus());
						break;
					}
				}	
			}			
		} catch (Exception e) {
			logger.error("Exception occurred while determining if payment option : " + paymentOptionCode + " is enabled", e);
		}
		logger.info("Is paymentOption : " + paymentOptionCode + " enabled : " + isPayOpEnabled);
		return isPayOpEnabled;
	}

	public Boolean isPaymentOptionEnabled(String merchantCode, String productCode, String paymentOptionCode) {

		Boolean isPayOpEnabled = null;
		try {
			// Get list of all payment options for merchant and product.
			String key = PaymentUIUtil.getPaymentDivKey(merchantCode, productCode);
			List<UIMerchantPayopMapping> merchantPayopMappingList = merchantPayOpCache.getAll(key);

			// Iterate over the list and determine if payment option with given code is enabled for given merchant and product.
			if(merchantPayopMappingList != null) {
				for (UIMerchantPayopMapping merchantPayopMapping : merchantPayopMappingList) {
					if (merchantPayopMapping != null && StringUtils.equalsIgnoreCase(merchantPayopMapping.getPaymentOption(), paymentOptionCode)) {
						isPayOpEnabled = PaymentUIUtil.isEnabled(merchantPayopMapping.getStatus());
						break;
					}
				}
			}			
		} catch (Exception e) {
			logger.error("Exception occurred while determining if payment option : " + paymentOptionCode + " is enabled for merchant " + merchantCode + " and product : " + productCode, e);
		}
		logger.info("Is paymentOption : " + paymentOptionCode + " enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		return isPayOpEnabled;
	}
	public Boolean isNonMoto(String merchantCode, String productCode, String paymentOptionCode) {

		Boolean isNonMoto = null;
		try {
			// Get list of all payment options for merchant and product.
			String key = PaymentUIUtil.getPaymentDivKey(merchantCode, productCode);
			List<UIMerchantPayopMapping> merchantPayopMappingList = merchantPayOpCache.getAll(key);

			// Iterate over the list and determine if payment option with given code is enabled for given merchant and product.
			if(merchantPayopMappingList != null) {
				for (UIMerchantPayopMapping merchantPayopMapping : merchantPayopMappingList) {
					if (merchantPayopMapping != null && StringUtils.equalsIgnoreCase(merchantPayopMapping.getPaymentOption(), paymentOptionCode)) {
						return merchantPayopMapping.isNonMoto();
					}
				}
			}			
		} catch (Exception e) {
			logger.error("Exception occurred while determining if payment option : " + paymentOptionCode + " is enabled for merchant " + merchantCode + " and product : " + productCode, e);
		}
		logger.info("Is paymentOption : " + paymentOptionCode + " enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isNonMoto);
		return isNonMoto;
	}
	
	public Boolean isCardTypeEnabled(String merchantCode, String productCode, String cardTypeCode) {
		
		Boolean isCardTypeEnabled = null;
		try {
			// Get list of all Card types for merchant and product.
			String key = PaymentUIUtil.getPaymentDivKey(merchantCode, productCode);
			List<UIProductCardTypeMapping> productCardTypeMappingList = productCardTypeMappingCache.getAll(key);
			
			// Iterate over the list and determine if card type with given code is enabled for given merchant and product.
			if(productCardTypeMappingList != null) {
				for(UIProductCardTypeMapping productCardTypeMapping : productCardTypeMappingList) {
					if(productCardTypeMapping != null && StringUtils.equals(productCardTypeMapping.getCardTypeCode(), cardTypeCode)) {
						isCardTypeEnabled = PaymentUIUtil.isEnabled(productCardTypeMapping.getStatus());
						break;
					}
				}
			}			
		} catch (Exception e) {
			logger.error("Exception occurred while determining if Card Type : " + cardTypeCode + " is enabled for merchant " + merchantCode + " and product : " + productCode, e);
		}
		logger.info("Is Card Type : " + cardTypeCode + " enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isCardTypeEnabled);
		return isCardTypeEnabled;
	}
	
	public Boolean isBankEnabled(String merchantCode, String productCode, String bankCode) {
		
		Boolean isBankEnabled = null;
		try {
			// Get list of all Banks for merchant and product.
			String key = PaymentUIUtil.getPaymentDivKey(merchantCode, productCode);
			List<UIProductBanksMapping> productBanksMappingList = productBanksCache.getAll(key);
			
			// Iterate over the list and determine if bank with given code is enabled for given merchant and product.
			if(productBanksMappingList != null) {
				for(UIProductBanksMapping productBanksMapping : productBanksMappingList) {
					if(productBanksMapping != null && StringUtils.equals(productBanksMapping.getBankCode(), bankCode)) {
						isBankEnabled = PaymentUIUtil.isEnabled(productBanksMapping.getStatus());
						break;
					}
				}
			}			
		} catch (Exception e) {
			logger.error("Exception occurred while determining if bank : " + bankCode + " is enabled for merchant " + merchantCode + " and product : " + productCode, e);
		}
		logger.info("Is Bank : " + bankCode + " enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isBankEnabled);
		return isBankEnabled;
	}
}
