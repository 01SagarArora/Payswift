package com.yatra.payment.ui.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.CreditCardPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("creditCardOptionBuilder")
public class CreditCardOptionBuilder extends CardOptionBuilder {

	private static Logger logger = Logger.getLogger(CreditCardOptionBuilder.class);

	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			// This API builds a CreditCardPaymentOption bean which is common for all merchants and products.
			CreditCardPaymentOption commonPaymentOption = new CreditCardPaymentOption(paymentOption);
			commonPaymentOption.setCardHolderNameLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cardHolderNameLabel"));
			commonPaymentOption.setCardNoLabel(getUIParamMappingValue(paymentOption.getCode(), COMMON_CONSTANT, "cardNoLabel"));
			commonPaymentOption.setBillingAddress(getUIBillingAddress());
			return commonPaymentOption;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building CreditCardOption " , e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting CC Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if CC is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD);
		logger.info("Is CC enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no CC entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if CC is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get CC PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common CC payment option bean exists. Returning null..");
			return null;
		}
		// Build CC PaymentOption bean for given merchant and product.
		commonPaymentOption.setNonMoto(statusProviderService.isNonMoto(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD));
		CreditCardPaymentOption creditCardPaymentOption = new CreditCardPaymentOption(commonPaymentOption);
		List<CardType> cardTypeList = getEnabledCardTypes(creditCardPaymentOption.getCode(), merchantCode, productCode);
		creditCardPaymentOption.setCardTypes(cardTypeList);
		creditCardPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning CC Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return creditCardPaymentOption;
	}
}