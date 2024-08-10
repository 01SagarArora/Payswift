package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.Pay4BizPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class Pay4BizOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(Pay4BizOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			Pay4BizPaymentOption pay4BizPaymentOption = new Pay4BizPaymentOption(paymentOption);
			return pay4BizPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building PAY4BIZ Payment Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting pay4Biz Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  pay4Biz payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PAY4BIZ);
		logger.info("Is pay4Biz enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no pay4Biz entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if pay4Biz enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  pay4Biz PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PAY4BIZ);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common pay4Biz option bean exists. Returning null..");
			return null;
		}		
		// Build Pay4BizPaymentOption bean for given merchant and product.
		Pay4BizPaymentOption pay4BizPaymentOption = new Pay4BizPaymentOption(commonPaymentOption);
		pay4BizPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning pay4Biz Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return pay4BizPaymentOption;

	}

}






