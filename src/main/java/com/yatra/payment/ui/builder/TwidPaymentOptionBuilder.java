package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.display.beans.TwidPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class TwidPaymentOptionBuilder implements PaymentOptionBuilder {

	private static final Logger logger = Logger.getLogger(TwidPaymentOptionBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;
	@Autowired private PayswiftStagesDAOImpl payswiftStagesDAOImpl;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			TwidPaymentOption twidPaymentOption = new TwidPaymentOption(paymentOption);
			return twidPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building TwidPaymentOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting TwidPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if OxigenB2B payment option is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_TWID);
		logger.info("Is Twid enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no OxigenB2B entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Twid enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get OxigenB2B PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_TWID);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Twid option bean exists. Returning null..");
			return null;
		}		
		// Build OxigenB2B PaymentOption bean for given merchant and product.
		TwidPaymentOption twidPaymentOption = new TwidPaymentOption(commonPaymentOption);
		twidPaymentOption.setSubText(payswiftStagesDAOImpl.getYatraPropertyValueFromDB("payop.twid.sub.text"));
		twidPaymentOption.setBalance(null);
		twidPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning TwidPaymentOption bean for merchant : " + merchantCode + " and product : " + productCode);

		return twidPaymentOption;

	}

}
