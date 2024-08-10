package com.yatra.payment.ui.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.EMIPaymentOption;
import com.yatra.payment.ui.display.beans.EmiBankComparator;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("emiOptionBuilder")
public class EMIOptionBuilder extends CardOptionBuilder {

	private static Logger logger = Logger.getLogger(EMIOptionBuilder.class);

	@Autowired	private UICacheManager<EMIBank> emiBanksCache;
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;

	protected static String COMMON_CONSTANT = "common";

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			EMIPaymentOption commonPaymentOption = new EMIPaymentOption(paymentOption);
			commonPaymentOption.setCardHolderNameLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cardHolderNameLabel"));
			commonPaymentOption.setCardNoLabel(getUIParamMappingValue(paymentOption.getCode(), COMMON_CONSTANT, "cardNoLabel"));
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building EMIOption " , e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting EMI Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if EMI is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_EMI);
		logger.info("Is EMI enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no EMI entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if EMI is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get EMI PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_EMI);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common EMI payment option bean exists. Returning null..");
			return null;
		}
		
		// Build EMI PaymentOption bean for given merchant and product.
		EMIPaymentOption emiPaymentOption = new EMIPaymentOption(commonPaymentOption);
		List<CardType> enabledCardTypes = getEnabledCardTypes(emiPaymentOption.getCode(), merchantCode, productCode);
		List<EMIBank> bankList = getEMIBankList(enabledCardTypes);
		emiPaymentOption.setBanks(bankList);
		emiPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning EMI Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return emiPaymentOption;
	}

	private List<EMIBank> getEMIBankList(List<CardType> enabledCardTypes) throws Exception {
		
		List<EMIBank> emiBankListForMerchant = new ArrayList<EMIBank>();

		// Get list of all EMI banks.
		List<EMIBank> emiBankList = emiBanksCache.getAll();
		if(emiBankList == null || emiBankList.isEmpty()) {
			logger.info("No EMI Banks found in EMI Bank Cache. Returning empty EMI bank list");
			return emiBankListForMerchant;
		}
		
		// Iterate over all EMI banks and create EMIBank bean containing enabled card types for this merchant.
		for(EMIBank emiBank : emiBankList) {
			
			// Get List of all card types which are enabled for this EMI bank.
			List<CardType> emiBankCardTypes = emiBank.getCardTypes();
			
			// Get List of all EMI bank card types enabled for given merchant.
			List<CardType> enabledEMIBankCardTypeList = getEnabledEMIBankCardTypes(emiBankCardTypes, enabledCardTypes);
			
			// Create EMIBank bean for merchant consisting of enabled card types and add it to list.
			EMIBank emiBankForMerchant = new EMIBank(emiBank);
			emiBankForMerchant.setCardTypes(enabledEMIBankCardTypeList);
			emiBankListForMerchant.add(emiBankForMerchant);
		}
		
		// Sort list and return.
		EmiBankComparator bankComparator = new EmiBankComparator();
		Collections.sort(emiBankListForMerchant, bankComparator);
		return emiBankListForMerchant;
	}
	
	private List<CardType> getEnabledEMIBankCardTypes(List<CardType> emiBankCardTypes, List<CardType> enabledCardTypes) {
		List<CardType> cardTypeList = new ArrayList<CardType>();
		for(CardType emiCardType : emiBankCardTypes) {
			for(CardType enabledCardType : enabledCardTypes) {
				if(emiCardType.getCode().equals(enabledCardType.getCode())) {
					cardTypeList.add(enabledCardType);
					break;
				}
			}
		}
		return cardTypeList;
	}
}
