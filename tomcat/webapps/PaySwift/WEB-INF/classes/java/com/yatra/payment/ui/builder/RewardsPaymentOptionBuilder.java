package com.yatra.payment.ui.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopSubTypesMappingCache;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.RewardPaymentOption;
import com.yatra.payment.ui.service.RewardBinsProvider;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("rewardsPaymentOptionBuilder")
public class RewardsPaymentOptionBuilder extends CardOptionBuilder {

	private static Logger logger = Logger.getLogger(RewardsPaymentOptionBuilder.class);

	@Autowired private PayopSubTypesMappingCache payopSubTypesMappingCache;
	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired private RewardBinsProvider rewardBinsProvider;

	private static String COMMON_CONSTANT = "common";

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			RewardPaymentOption commonPaymentOption = new RewardPaymentOption(paymentOption);
			commonPaymentOption.setCardHolderNameLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cardHolderNameLabel"));
			commonPaymentOption.setCardNoLabel(getUIParamMappingValue(paymentOption.getCode(), COMMON_CONSTANT, "cardNoLabel"));
			commonPaymentOption.setRewardPointLabel(getUIParamMappingValue(commonPaymentOption.getCode(), COMMON_CONSTANT, "rwPointLabel"));
			commonPaymentOption.setRewardPointValueLabel(getUIParamMappingValue(commonPaymentOption.getCode(), COMMON_CONSTANT, "rwPointValueLabel"));
			commonPaymentOption.setRewardBinList(rewardBinsProvider.getRewardCardBinList());
			UIPayopSubTypesMapping payopSubTypesMapping = payopSubTypesMappingCache.get(paymentOption.getCode());
			if (payopSubTypesMapping != null) {
				commonPaymentOption.setBankLogoURL(payopSubTypesMapping.getImageURL());
			}
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Rewards Payment Option " , e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting Rewards Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Rewards is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_REWARDS);
		logger.info("Is Rewards enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Rewards entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Rewards is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Rewards PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_REWARDS);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Rewards payment option bean exists. Returning null..");
			return null;
		}
		
		// Build Rewards PaymentOption bean for given merchant and product.
		RewardPaymentOption rewardPaymentOption = new RewardPaymentOption(commonPaymentOption);
		List<CardType> cardTypeList = getEnabledCardTypes(rewardPaymentOption.getCode(), merchantCode, productCode);
		rewardPaymentOption.setCardTypes(cardTypeList);
		rewardPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Rewards Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return rewardPaymentOption;
	}

}
