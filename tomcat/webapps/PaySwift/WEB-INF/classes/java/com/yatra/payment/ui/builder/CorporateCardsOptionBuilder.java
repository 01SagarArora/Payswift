package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CorporateCardPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("corporateCardOptionBuilder")
public class CorporateCardsOptionBuilder implements PaymentOptionBuilder{
	private static Logger logger = Logger.getLogger(CorporateCardsOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			CorporateCardPaymentOption paymentOptionCorporateCard= new CorporateCardPaymentOption(paymentOption);
			return paymentOptionCorporateCard;

		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Corporate Card Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		logger.info("Getting Corporate Card Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if Net Banking is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD);
		logger.info("Is Corporate Card enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Corporate Card enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Corporate Card option bean exists. Returning null..");
			return null;
		}
		
		// Build Corporate Card PaymentOption bean for given merchant and product.
		CorporateCardPaymentOption corporateCardPaymentOption = new CorporateCardPaymentOption(commonPaymentOption);
		corporateCardPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Corporate Card Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return corporateCardPaymentOption;

	}

}
