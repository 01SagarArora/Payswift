package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.TSIWalletPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("tsiWalletPayOptionBuilder")
public class TSIWalletPayOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(TSIWalletPayOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			TSIWalletPaymentOption tsiWalletPaymentOption = new TSIWalletPaymentOption(paymentOption);
			return tsiWalletPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building TSIWallet Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting TSIWallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  BankIt payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_TSIWALLET);
		logger.info("Is TSIWallet enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no BankIt entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if TSIWallet enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  BankIt PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_TSIWALLET);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common TSIWallet option bean exists. Returning null..");
			return null;
		}		
		// Build BankIt PaymentOption bean for given merchant and product.
		TSIWalletPaymentOption tsiWalletPaymentOption = new TSIWalletPaymentOption(commonPaymentOption);
		tsiWalletPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning TSIWallet Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return tsiWalletPaymentOption;

	}

}






