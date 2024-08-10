package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.ZestMoneyPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class ZestMoneyOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(ZestMoneyOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			ZestMoneyPaymentOption zestMoneyPaymentOption = new ZestMoneyPaymentOption(paymentOption);
			return zestMoneyPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building ZestMoney Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting ZestMoney Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  ZestMoney payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_ZESTMONEY);
		logger.info("Is ZestMoney enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no ZestMoney entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if ZestMoney enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  ZestMoney PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_ZESTMONEY);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common ZestMoney option bean exists. Returning null..");
			return null;
		}		
		// Build ZestMoney PaymentOption bean for given merchant and product.
		ZestMoneyPaymentOption zestMoneyPaymentOption = new ZestMoneyPaymentOption(commonPaymentOption);
		zestMoneyPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));

		return zestMoneyPaymentOption;

	}

}





