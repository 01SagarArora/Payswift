package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.VakrangeePaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class VakrangeeOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(VakrangeeOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			VakrangeePaymentOption vakrangeePaymentOption = new VakrangeePaymentOption(paymentOption);
			return vakrangeePaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building VakrangeePaymentOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting VakrangeePaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  vakrangee payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_VAKRANGEE);
		logger.info("Is vakrangee enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no vakrangee entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if vakrangee enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  vakrangee PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_VAKRANGEE);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common vakrangee option bean exists. Returning null..");
			return null;
		}		
		// Build VakrangeePaymentOption bean for given merchant and product.
		VakrangeePaymentOption vakrangeePaymentOption = new VakrangeePaymentOption(commonPaymentOption);
		vakrangeePaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning vakrangeePaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		return vakrangeePaymentOption;

	}

}
