package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.GooglePayUPIPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class GooglePayUPIOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(GooglePayUPIOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			GooglePayUPIPaymentOption googlePayUPIPaymentOption = new GooglePayUPIPaymentOption(paymentOption);
			return googlePayUPIPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Google Tez Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting GooglePay UPI Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if OxigenB2B payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_GOOGLE_PAY_UPI);
		logger.info("Is Google Tez enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no OxigenB2B entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if GooglePay UPI enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get OxigenB2B PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_PAY_UPI);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common GooglePay UPI option bean exists. Returning null..");
			return null;
		}		
		// Build OxigenB2B PaymentOption bean for given merchant and product.
		GooglePayUPIPaymentOption googlePayUPIPaymentOption = new GooglePayUPIPaymentOption(commonPaymentOption);
		googlePayUPIPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning GooglePayUPIPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		return googlePayUPIPaymentOption;

	}

}

