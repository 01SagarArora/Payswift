package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.RoinetPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class RoinetOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(RoinetOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			RoinetPaymentOption roinetPaymentOption = new RoinetPaymentOption(paymentOption);
			return roinetPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building RoinetPaymentOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting roinet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  roinet payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_ROINET);
		logger.info("Is roinet enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no roinet entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if roinet enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  roinet PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_ROINET);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common roinet option bean exists. Returning null..");
			return null;
		}		
		// Build RoinetPaymentOption bean for given merchant and product.
		RoinetPaymentOption roinetPaymentOption = new RoinetPaymentOption(commonPaymentOption);
		roinetPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning RoinetPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		return roinetPaymentOption;

	}

}






