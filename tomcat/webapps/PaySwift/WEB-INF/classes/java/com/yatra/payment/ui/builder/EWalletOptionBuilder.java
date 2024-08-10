package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.EWalletPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("eWalletOptionBuilder")
public class EWalletOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(EWalletOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			EWalletPaymentOption commonPaymentOption = new EWalletPaymentOption(paymentOption);
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building EWalletOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting eWallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if eWallet is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_EWALLET);
		logger.info("Is eWallet enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no eWallet entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if eWallet is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get eWallet PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_EWALLET);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common eWallet payment option bean exists. Returning null..");
			return null;
		}
		
		// Build eWallet PaymentOption bean for given merchant and product.
		EWalletPaymentOption eWalletPaymentOption = new EWalletPaymentOption(commonPaymentOption);
		eWalletPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning eWallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return eWalletPaymentOption;
	}
}
