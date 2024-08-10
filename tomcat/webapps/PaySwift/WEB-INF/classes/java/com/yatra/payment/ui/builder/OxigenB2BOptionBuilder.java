package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.OxigenB2BPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class OxigenB2BOptionBuilder implements PaymentOptionBuilder {
	
	private static final Logger logger = Logger.getLogger(OxigenB2BOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	
	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			OxigenB2BPaymentOption oxigenB2BPaymentOption = new OxigenB2BPaymentOption(paymentOption);
			return oxigenB2BPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building OxigenB2B Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting OxigenB2B Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if OxigenB2B payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_OXIGEN_B2B);
		logger.info("Is OxigenB2B enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no OxigenB2B entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if OxigenB2B enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get OxigenB2B PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_OXIGEN_B2B);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common OxigenB2B option bean exists. Returning null..");
			return null;
		}		
		// Build OxigenB2B PaymentOption bean for given merchant and product.
		OxigenB2BPaymentOption oxigenB2BPaymentOption = new OxigenB2BPaymentOption(commonPaymentOption);
		oxigenB2BPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning OxigenB2B Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return oxigenB2BPaymentOption;

	}

}
