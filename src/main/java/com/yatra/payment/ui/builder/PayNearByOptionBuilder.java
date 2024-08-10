package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PayNearByPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class PayNearByOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(PayNearByOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			PayNearByPaymentOption payNearBypaymentOption = new PayNearByPaymentOption(paymentOption);
			return payNearBypaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building PayNearBy Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting PayNearBy Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  BankIt payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PAYNEARBY);
		logger.info("Is PayNearBy enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no BankIt entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if PayNearBy enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  BankIt PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PAYNEARBY);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common PayNearBy option bean exists. Returning null..");
			return null;
		}		
		// Build BankIt PaymentOption bean for given merchant and product.
		PayNearByPaymentOption payNearByPaymentOption = new PayNearByPaymentOption(commonPaymentOption);
		payNearByPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning PayNearBy Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return payNearByPaymentOption;

	}

}





