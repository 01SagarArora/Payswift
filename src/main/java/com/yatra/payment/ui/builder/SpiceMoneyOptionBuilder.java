package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.SpiceMoneyPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class SpiceMoneyOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(BankItOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			SpiceMoneyPaymentOption spiceMoneyPaymentOption = new SpiceMoneyPaymentOption(paymentOption);
			return spiceMoneyPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building BankIt Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting SpiceMoney Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  SpiceMoney payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_SPICEMONEY);
		logger.info("Is SpiceMoney enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no BankIt entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if SpiceMoney enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  SpiceMoney PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_SPICEMONEY);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common SpiceMoney option bean exists. Returning null..");
			return null;
		}		
		// Build SpiceMoneyPaymentOption bean for given merchant and product.
		SpiceMoneyPaymentOption spiceMoneyPaymentOption = new SpiceMoneyPaymentOption(commonPaymentOption);
		spiceMoneyPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning SpiceMoney Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return spiceMoneyPaymentOption;

	}

}





