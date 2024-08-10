package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CashPoolPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;


@Service
public class CashPoolPayOpBuilder implements PaymentOptionBuilder{

	private static Logger logger = Logger.getLogger(CashPoolPayOpBuilder.class);

	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) {
		logger.info("Entering into build cash pool payment option");
		CashPoolPaymentOption payOp = new CashPoolPaymentOption(paymentOption); 
		logger.info("Exiting from build cash pool payment option");
		return payOp;
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		logger.info("Entring into build cash pool payment option for merchant code "+merchantCode+" product code "+productCode);

		// Check if Cash Pool is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_CASHPOOL);
		logger.info("Is Cash Pool enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Cash Pool entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Cash Pool enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}

		// Get Cash Pool PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_CASHPOOL);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Cash Pool option bean exists. Returning null..");
			return null;
		}
		
		CashPoolPaymentOption paymentOption = new CashPoolPaymentOption(commonPaymentOption);
		paymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Cash Pool Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return paymentOption;
	}

}
