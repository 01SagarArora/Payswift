package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.JaldiCashPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class JaldiCashOptionBuilder implements PaymentOptionBuilder {
	
	private static final Logger logger = Logger.getLogger(JaldiCashOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	
	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			JaldiCashPaymentOption jaldiCashpaymentOption = new JaldiCashPaymentOption(paymentOption);
			return jaldiCashpaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Jaldi Cash Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting Jaldi Cash Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Jaldi Cash payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_JCASH);
		logger.info("Is Jaldi Cash enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Jaldi Cash entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Jaldi Cash enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Jaldi Cash PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_JCASH);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Jaldi Cash option bean exists. Returning null..");
			return null;
		}		
		// Build Jaldi Cash PaymentOption bean for given merchant and product.
		JaldiCashPaymentOption jaldiCashPaymentOption = new JaldiCashPaymentOption(commonPaymentOption);
		jaldiCashPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Jaldi Cash Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return jaldiCashPaymentOption;
		
	}

}
