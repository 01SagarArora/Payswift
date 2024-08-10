package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.UpiPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("upiPaymentOptionBuilder")
public class UpiPaymentOptionBuilder implements PaymentOptionBuilder {

private static Logger logger = Logger.getLogger(UpiPaymentOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			UpiPaymentOption paymentOptionUpi= new UpiPaymentOption(paymentOption);
			return paymentOptionUpi;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building UPI Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
	logger.info("Getting UPI Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Net Banking is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_UPI);
		logger.info("Is UPI enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if UPI enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_UPI);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common UPI option bean exists. Returning null..");
			return null;
		}

		// Build UPI PaymentOption bean for given merchant and product.
		UpiPaymentOption upiPaymentOption = new UpiPaymentOption(commonPaymentOption);
		upiPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning UPI Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return upiPaymentOption;
		
	}

}
