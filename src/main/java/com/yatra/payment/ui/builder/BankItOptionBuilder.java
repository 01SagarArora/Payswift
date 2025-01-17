package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.BankItPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class BankItOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(BankItOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			BankItPaymentOption bankItpaymentOption = new BankItPaymentOption(paymentOption);
			return bankItpaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building BankIt Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting BankIt Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  BankIt payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_BANKIT);
		logger.info("Is BankIt enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no BankIt entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if BankIt enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  BankIt PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_BANKIT);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common BankIt option bean exists. Returning null..");
			return null;
		}		
		// Build BankIt PaymentOption bean for given merchant and product.
		BankItPaymentOption bankItPaymentOption = new BankItPaymentOption(commonPaymentOption);
		bankItPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning BankIt Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return bankItPaymentOption;

	}

}




