package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopSubTypesMappingCache;
import com.yatra.payment.cache.service.impl.PayopSuboptionMappingCache;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.PayLaterPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class PayLaterOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(PayLaterOptionBuilder.class);

	@Autowired private PayopSubTypesMappingCache payopSubTypesMappingCache;
	@Autowired private PayopSuboptionMappingCache payopSuboptionMappingCache;
	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			PayLaterPaymentOption commonPaymentOption = new PayLaterPaymentOption(paymentOption);
			UIPayopSubTypesMapping payopSubTypesMapping = payopSubTypesMappingCache.get(paymentOption.getCode());
			if (payopSubTypesMapping != null) {
//				commonPaymentOption.setName(payopSubTypesMapping.getDisplayName());
//				commonPaymentOption.setWalletCode(payopSubTypesMapping.getOptionCode());
//				commonPaymentOption.setLogoU(payopSubTypesMapping.getImageURL());
				commonPaymentOption.setSuboptionList(payopSuboptionMappingCache.get(payopSubTypesMapping.getPaymentOption()));
			}
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building PayLaterPaymentOption " , e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting Pay Later Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Pay Later is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PAY_LATER);
		logger.info("Is Pay Later enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no mobile Wallet entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Mobile Wallet is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Pay Later PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PAY_LATER);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Pay Later payment option bean exists. Returning null..");
			return null;
		}
		
		// Build PayLater PaymentOption bean for given merchant and product.
		PayLaterPaymentOption payLaterPaymentOption = new PayLaterPaymentOption(commonPaymentOption);
		payLaterPaymentOption.setSuboptionList(payopSuboptionMappingCache.get(payLaterPaymentOption.getCode() + "_" + merchantCode + "_" + productCode));
		payLaterPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Mobile Wallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return payLaterPaymentOption;
	}
}
