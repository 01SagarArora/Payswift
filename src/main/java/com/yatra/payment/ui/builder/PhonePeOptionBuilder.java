package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopSubTypesMappingCache;
import com.yatra.payment.cache.service.impl.PayopSuboptionMappingCache;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.AmazonPayPaymentOption;
import com.yatra.payment.ui.display.beans.PhonePePaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class PhonePeOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(PhonePeOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;
	@Autowired private PayopSubTypesMappingCache payopSubTypesMappingCache;
	@Autowired private PayopSuboptionMappingCache payopSuboptionMappingCache;


	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			PhonePePaymentOption phonePePaymentOption = new PhonePePaymentOption(paymentOption);
			UIPayopSubTypesMapping payopSubTypesMapping = payopSubTypesMappingCache.get(paymentOption.getCode());
			if (payopSubTypesMapping != null) {
				phonePePaymentOption.setSuboptionList(payopSuboptionMappingCache.get(payopSubTypesMapping.getPaymentOption()));
			}
			return phonePePaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building PhonePePaymentOption " , e);
			throw e;
		}

	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting PhonePe Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if  BankIt payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_PHONEPE);
		logger.info("Is PhonePe enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no BankIt entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if PhonePe enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get  BankIt PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_PHONEPE);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common PhonePe option bean exists. Returning null..");
			return null;
		}		
		// Build BankIt PaymentOption bean for given merchant and product.
		PhonePePaymentOption phonePePaymentOption = new PhonePePaymentOption(commonPaymentOption);
		phonePePaymentOption.setSuboptionList(payopSuboptionMappingCache.get(phonePePaymentOption.getCode() + "_" + merchantCode + "_" + productCode));
		phonePePaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning PhonePe Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return phonePePaymentOption;

	}

}







