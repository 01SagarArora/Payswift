package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.PaypalPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("paypalPaymentOptionBuilder")
public class PaypalOptionBuilder implements PaymentOptionBuilder {
	private static Logger logger = Logger.getLogger(PaypalOptionBuilder.class);

	@Autowired
	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired
	protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			PaypalPaymentOption paymentOptionPaypal = new PaypalPaymentOption(paymentOption);
			return paymentOptionPaypal;

		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Paypal Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		// Check if Paypal is enabled for given merchant and product.
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PAYPAL);
		logger.info("Is Paypal enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Net Banking entry
		// exists for given merchant and product, so return null.
		if (isPayOpEnabled == null) {
			logger.info("Could not be determined if Paypal enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Paypal PaymentOption bean which is common for all merchants
		// and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PAYPAL);

		// If commonPaymentOption = null, return null.
		if (commonPaymentOption == null) {
			logger.info("No common Paypal option bean exists. Returning null..");
			return null;
		}

		// Build Paypal PaymentOption bean for given merchant and product.
		PaypalPaymentOption paypalPaymentOption = new PaypalPaymentOption(commonPaymentOption);
		paypalPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Paypal Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return paypalPaymentOption;
	}

}
