package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PayMonkPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class PayMonkOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(PayMonkOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			PayMonkPaymentOption payMonkPaymentOption = new PayMonkPaymentOption(paymentOption);
			return payMonkPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building PayMonk Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting PayMonk Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  PayMonk payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PAYMONK);
		logger.info("Is PayMonk enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no PayMonk entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if PayMonk enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  PayMonk PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PAYMONK);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common PayMonk option bean exists. Returning null..");
			return null;
		}		
		// Build PayMonk PaymentOption bean for given merchant and product.
		PayMonkPaymentOption payMonkPaymentOption = new PayMonkPaymentOption(commonPaymentOption);
		payMonkPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning PayMonk Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return payMonkPaymentOption;

	}

}





