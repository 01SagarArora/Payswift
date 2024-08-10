package com.yatra.payment.ui.builder;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.PayuPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("payuOptionBuilder")
public class PayuOptionBuilder implements PaymentOptionBuilder{
	
	private static Logger logger = Logger.getLogger(PayuOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			PayuPaymentOption paymentOptionPayu= new PayuPaymentOption(paymentOption);
			return paymentOptionPayu;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Payu Banking Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
	logger.info("Getting Payu  Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Net Banking is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PAYU);
		logger.info("Is Payu enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Payu enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PAYU);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Payu option bean exists. Returning null..");
			return null;
		}
		// Build Payu PaymentOption bean for given merchant and product.
		PayuPaymentOption payuPaymentOption = new PayuPaymentOption(commonPaymentOption);
		payuPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Payu Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return payuPaymentOption;		
	}

}
