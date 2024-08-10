package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.EbixCashPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class EbixCashOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(EbixCashOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			EbixCashPaymentOption ebixCashPaymentOption = new EbixCashPaymentOption(paymentOption);
			return ebixCashPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building EbixCashPaymentOption", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting EbixCashPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  EbixCashPaymentOption is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_EBIXCASH);
		logger.info("Is EbixCash enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no EbixCashPaymentOption entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if EbixCash enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  EbixCashPaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_EBIXCASH);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common EbixCash option bean exists. Returning null..");
			return null;
		}		
		// Build EbixCashPaymentOption bean for given merchant and product.
		EbixCashPaymentOption ebixCashPaymentOption = new EbixCashPaymentOption(commonPaymentOption);
		ebixCashPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning EbixCashPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		return ebixCashPaymentOption;

	}

}




