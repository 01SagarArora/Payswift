package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.ATMCardPaymentOption;
import com.yatra.payment.ui.display.beans.Bank;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("atmCardOptionBuilder")
public class ATMCardOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(ATMCardOptionBuilder.class);

	@Autowired	private UICacheManager<Bank> atmBankCache;
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			ATMCardPaymentOption commonPaymentOption = new ATMCardPaymentOption(paymentOption);
			setBankList(commonPaymentOption);
			return commonPaymentOption;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building ATMCardOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting ATM Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if ATM is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_ATM_CARD);
		logger.info("Is ATM enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no ATM entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if ATM is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get ATM PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_ATM_CARD);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common ATM payment option bean exists. Returning null..");
			return null;
		}
		
		// Build ATM PaymentOption bean for given merchant and product.
		ATMCardPaymentOption atmCardPaymentOption = new ATMCardPaymentOption(commonPaymentOption);
		atmCardPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning ATM Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return atmCardPaymentOption;
	}
	
	private void setBankList(ATMCardPaymentOption atmCardPaymentOption) {
		atmCardPaymentOption.setBanks(atmBankCache.getAll());
	}
}
