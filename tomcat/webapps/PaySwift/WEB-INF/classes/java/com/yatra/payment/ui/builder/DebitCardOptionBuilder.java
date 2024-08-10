package com.yatra.payment.ui.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.DebitCardPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("debitCardOptionBuilder")
public class DebitCardOptionBuilder extends CardOptionBuilder {
	
	private static Logger logger = Logger.getLogger(DebitCardOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			DebitCardPaymentOption commonPaymentOption = new DebitCardPaymentOption(paymentOption);
			commonPaymentOption.setCardHolderNameLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cardHolderNameLabel"));
			commonPaymentOption.setCardNoLabel(getUIParamMappingValue(paymentOption.getCode(), COMMON_CONSTANT, "cardNoLabel"));
			commonPaymentOption.setBillingAddress(getUIBillingAddress());
			return commonPaymentOption;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building DebitCardOption " , e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting DC Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if DC is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD);
		logger.info("Is DC enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no DC entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if DC is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get DC PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD);
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common DC payment option bean exists. Returning null..");
			return null;
		}
		commonPaymentOption.setNonMoto(statusProviderService.isNonMoto(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD));
		// Build DC PaymentOption bean for given merchant and product.
		DebitCardPaymentOption debitCardPaymentOption = new DebitCardPaymentOption(commonPaymentOption);
		List<CardType> cardTypeList = getEnabledCardTypes(debitCardPaymentOption.getCode(), merchantCode, productCode);
		debitCardPaymentOption.setCardTypes(cardTypeList);
		debitCardPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning DC Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return debitCardPaymentOption;
	}
}
