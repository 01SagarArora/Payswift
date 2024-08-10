package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.QuicksonPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("quicksonOptionBuilder")
public class QuicksonOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(QuicksonOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			return new QuicksonPaymentOption(paymentOption);
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Quickson Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting Quickson Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  Quickson payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_QUICKSON);
		logger.info("Is Quickson enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Quickson entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Quickson enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  Quickson PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_QUICKSON);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Quickson option bean exists. Returning null..");
			return null;
		}		
		// Build quicksonPaymentOption bean for given merchant and product.
		QuicksonPaymentOption quicksonPaymentOption = new QuicksonPaymentOption(commonPaymentOption);
		quicksonPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Quickson Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return quicksonPaymentOption;
	}
}