package com.yatra.payment.ui.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.builder.PaymentOptionBuilder;
import com.yatra.payment.ui.builder.PaymentOptionBuilderFactory;
import com.yatra.payment.ui.display.beans.PaymentDiv;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOptionComparator;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("paymentDivGenerator")
public class PaymentDivGenerator {
	
	private static Logger logger = Logger.getLogger(PaymentDivGenerator.class);
	
	@Autowired	private PaymentOptionBuilderFactory paymentOptionBuilderFactory;
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	
	public PaymentDiv generatePaymentDiv(String merchantCode, String productCode) {
		logger.info("Building Payment Div Cache for merchantCode : " + merchantCode + " and productCode : " + productCode);
		PaymentDiv paymentDiv = new PaymentDiv();
		paymentDiv.setMerchantCode(merchantCode);
		paymentDiv.setProductCode(productCode);
		paymentDiv.setPaymentOptions(buildPaymentOptions(merchantCode, productCode));
		return paymentDiv;
	}
	
	private List<PaymentOption> buildPaymentOptions(String merchantCode, String productCode) {

		logger.info("Building Payment Options for merchantCode : " + merchantCode + " and productCode : " + productCode);

		// Get all payment options
		List<PaymentOption> paymentOptionList = paymentOptionCache.getAll();

		// If no payment options retrieved, return an empty list.
		if (paymentOptionList == null || paymentOptionList.isEmpty()) {
			logger.error("No payment options exist. PaymentOptionCache returned an empty list.");
			return new ArrayList<PaymentOption>();
		}
		// If payment options are retrieved, create PaymentOption List for given merchant and product.
		List<PaymentOption> payOpListForMerchant = new ArrayList<PaymentOption>();
		
		// Iterate over all payment options, and create corresponding PaymentOption bean for given merchant and product.
		for (PaymentOption paymentOption : paymentOptionList) {
			try {
				// Check if payment Option is enabled.
				boolean isPaymentOptionEnabled = PaymentUIUtil.isEnabled(paymentOption.getStatus());
				logger.info("Is paymentOption with code : " + paymentOption.getCode() + " enabled : " + isPaymentOptionEnabled);
				
				// If enabled, get corresponding bean for given merchant and product.
				if(isPaymentOptionEnabled) {
					
					// Get PaymentOptionBuilder that will be used to build Payment Option bean for given merchant and product.
					PaymentOptionBuilder paymentOptionBuilder = paymentOptionBuilderFactory.createPaymentOptionBuilder(paymentOption.getCode());
					
					// Build bean.
					if(paymentOptionBuilder != null) {
						PaymentOption payOpForMerchant = paymentOptionBuilder.build(merchantCode, productCode);
						if(payOpForMerchant != null && PaymentUIUtil.isEnabled(payOpForMerchant.getStatus())) {
							payOpListForMerchant.add(payOpForMerchant);
						}
					} else {
						logger.error("No PaymentOptionBuilder exists for payOp : " + paymentOption.getCode());
					}
				}
				
			} catch (Exception e) {
				logger.error("Exception occurred while building payment option bean for payOp : " + paymentOption.getCode() + " for merchant : " + merchantCode + " and product : " + productCode, e);
			}
		}
		// Sort PaymentOption List and return.
		Collections.sort(payOpListForMerchant, new PaymentOptionComparator());
		logger.info("Payment Options added for merchantCode : " + merchantCode + " and productCode : " + productCode);
		return payOpListForMerchant;
	}
}
