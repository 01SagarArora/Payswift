package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopSubTypesMappingCache;
import com.yatra.payment.cache.service.impl.PayopSuboptionMappingCache;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.PayLaterPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.AmazonPayPaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("amazonPayOptionBuilder")
public class AmazonPayOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(AmazonPayOptionBuilder.class);

	@Autowired private PayopSubTypesMappingCache payopSubTypesMappingCache;
	@Autowired private PayopSuboptionMappingCache payopSuboptionMappingCache;
	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			AmazonPayPaymentOption paymentOptionAmpy = new AmazonPayPaymentOption(paymentOption);
			UIPayopSubTypesMapping payopSubTypesMapping = payopSubTypesMappingCache.get(paymentOption.getCode());
			if (payopSubTypesMapping != null) {
				paymentOptionAmpy.setSuboptionList(payopSuboptionMappingCache.get(payopSubTypesMapping.getPaymentOption()));
			}
			return paymentOptionAmpy;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building PayLaterPaymentOption " , e);
			throw e;
		}

	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		logger.info("Getting AmazonPay Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if Net Banking is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_AMAZONPAY);
		logger.info("Is AmazonPay enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if AmazonPay enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_AMAZONPAY);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common AmazonPay option bean exists. Returning null..");
			return null;
		}
		// Build AmazonPay PaymentOption bean for given merchant and product.
		AmazonPayPaymentOption amazonPayPaymentOption = new AmazonPayPaymentOption(commonPaymentOption);
		amazonPayPaymentOption.setSuboptionList(payopSuboptionMappingCache.get(amazonPayPaymentOption.getCode() + "_" + merchantCode + "_" + productCode));
		amazonPayPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning AmazonPay Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return amazonPayPaymentOption;
		
	}

}

