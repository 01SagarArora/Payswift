package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.UseLocatorPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class UseLocatorOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(UseLocatorOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			UseLocatorPaymentOption useLocatorPaymentOption = new UseLocatorPaymentOption(paymentOption);
			return useLocatorPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building UseLocatorOptionBuilder ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting UseLocatorPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  useLocator payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_USELOCATOR);
		logger.info("Is useLocator enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no useLocator entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if useLocator enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  useLocator PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_USELOCATOR);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common UseLocatorPaymentOption bean exists. Returning null..");
			return null;
		}		
		// Build UseLocatorPaymentOption bean for given merchant and product.
		UseLocatorPaymentOption useLocatorPaymentOption = new UseLocatorPaymentOption(commonPaymentOption);
		useLocatorPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning UseLocatorPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		return useLocatorPaymentOption;

	}

}






