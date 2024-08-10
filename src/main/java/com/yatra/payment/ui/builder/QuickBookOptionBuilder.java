package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("quickBookOptionBuilder")
public class QuickBookOptionBuilder implements PaymentOptionBuilder {

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;
	
	private static Logger logger = Logger.getLogger(QuickBookOptionBuilder.class);

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			QuickBookPaymentOption commonPaymentOption = new QuickBookPaymentOption(paymentOption);
			return commonPaymentOption;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building QuickBookOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting Quick Book Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Quick Book is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK);
		logger.info("Is Quick Book enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Quick Book entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Quick Book is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Quick Book PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Quick Book payment option bean exists. Returning null..");
			return null;
		}
		
		// Build Quick Book PaymentOption bean for given merchant and product.
		QuickBookPaymentOption quickBookPaymentOption = new QuickBookPaymentOption(commonPaymentOption);
		quickBookPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Quick Book Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return quickBookPaymentOption;
	}
}
