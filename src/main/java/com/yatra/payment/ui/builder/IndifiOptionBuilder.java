package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.IndifiPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("indifiPaymentOptionBuilder")
public class IndifiOptionBuilder implements PaymentOptionBuilder {
	private static Logger logger = Logger.getLogger(IndifiOptionBuilder.class);

	@Autowired
	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired
	protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			IndifiPaymentOption paymentOptionindifi = new IndifiPaymentOption(paymentOption);
			return paymentOptionindifi;

		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Indifi Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		// Check if Indifi is enabled for given merchant and product.
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_INDIFI);
		logger.info("Is Indifi enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Net Banking entry
		// exists for given merchant and product, so return null.
		if (isPayOpEnabled == null) {
			logger.info("Could not be determined if Indifi enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Indifi PaymentOption bean which is common for all merchants
		// and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_INDIFI);

		// If commonPaymentOption = null, return null.
		if (commonPaymentOption == null) {
			logger.info("No common Indifi option bean exists. Returning null..");
			return null;
		}

		// Build Indifi PaymentOption bean for given merchant and product.
		IndifiPaymentOption indifiPaymentOption = new IndifiPaymentOption(commonPaymentOption);
		indifiPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning indifi Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return indifiPaymentOption;
	}

}
