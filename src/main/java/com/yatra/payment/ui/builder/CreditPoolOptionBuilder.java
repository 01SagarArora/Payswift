package com.yatra.payment.ui.builder;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CreditPoolPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("creditPoolOptionBuilder")
public class CreditPoolOptionBuilder implements PaymentOptionBuilder{
	
	private static Logger logger = Logger.getLogger(CreditPoolOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			CreditPoolPaymentOption paymentOptionCreditPool= new CreditPoolPaymentOption(paymentOption);
			return paymentOptionCreditPool;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Credit Pool Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
	logger.info("Getting Credit Pool Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Net Banking is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_CREDIT_POOL);
		logger.info("Is Credit Pool enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Credit Pool enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_CREDIT_POOL);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Credit Pool option bean exists. Returning null..");
			return null;
		}
		// Build Credit Pool PaymentOption bean for given merchant and product.
		CreditPoolPaymentOption creditPoolPaymentOption = new CreditPoolPaymentOption(commonPaymentOption);
		creditPoolPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Credit Pool Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return creditPoolPaymentOption;
		
	}

}
