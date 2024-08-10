package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopSubTypesMappingCache;
import com.yatra.payment.cache.service.impl.PayopSuboptionMappingCache;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.MobileWalletPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("mobileWalletOptionBuilder")
public class MobileWalletOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(MobileWalletOptionBuilder.class);

	@Autowired	private PayopSubTypesMappingCache payopSubTypesMappingCache;
	@Autowired	private PayopSuboptionMappingCache payopSuboptionMappingCache;
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			MobileWalletPaymentOption commonPaymentOption = new MobileWalletPaymentOption(paymentOption);
			UIPayopSubTypesMapping payopSubTypesMapping = payopSubTypesMappingCache.get(paymentOption.getCode());
			if (payopSubTypesMapping != null) {
				commonPaymentOption.setWalletName(payopSubTypesMapping.getDisplayName());
				commonPaymentOption.setWalletCode(payopSubTypesMapping.getOptionCode());
				commonPaymentOption.setLogoURL(payopSubTypesMapping.getImageURL());
				commonPaymentOption.setSuboptionList(payopSuboptionMappingCache.get(payopSubTypesMapping.getPaymentOption()));
			}
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building MobileWalletOption " , e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting Mobile Wallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if mobile Wallet is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_MOBILE_WALLET);
		logger.info("Is Mobile Wallet enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no mobile Wallet entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Mobile Wallet is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get mobile Wallet PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_MOBILE_WALLET);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Mobile Wallet payment option bean exists. Returning null..");
			return null;
		}
		
		// Build mobile Wallet PaymentOption bean for given merchant and product.
		MobileWalletPaymentOption mobileWalletPaymentOption = new MobileWalletPaymentOption(commonPaymentOption);
		mobileWalletPaymentOption.setSuboptionList(payopSuboptionMappingCache.get(mobileWalletPaymentOption.getCode()+"_"+merchantCode+"_"+productCode));
		mobileWalletPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Mobile Wallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return mobileWalletPaymentOption;
	}
}
