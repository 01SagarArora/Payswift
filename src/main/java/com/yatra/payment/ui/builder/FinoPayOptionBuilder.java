package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.FinoPayPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class FinoPayOptionBuilder implements PaymentOptionBuilder {
	
	private static final Logger logger = Logger.getLogger(FinoPayOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	
	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			FinoPayPaymentOption finoPayPaymentOption = new FinoPayPaymentOption(paymentOption);
			return finoPayPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building FinoPay Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting FinoPay Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if FinoPay payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_FINOPAY);
		logger.info("Is FinoPay enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no FinoPay entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if FinoPay enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get FinoPay PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_FINOPAY);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common FinoPay option bean exists. Returning null..");
			return null;
		}		
		// Build FinoPay PaymentOption bean for given merchant and product.
		FinoPayPaymentOption finoPayPaymentOption = new FinoPayPaymentOption(commonPaymentOption);
		finoPayPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning FinoPay Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return finoPayPaymentOption;

	}

}