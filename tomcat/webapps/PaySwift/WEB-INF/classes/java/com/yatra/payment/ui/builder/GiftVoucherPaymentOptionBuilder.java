package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.GiftVoucherPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("gvPaymentOptionBuilder")
public class GiftVoucherPaymentOptionBuilder implements PaymentOptionBuilder{
	
private static Logger logger = Logger.getLogger(GiftVoucherPaymentOptionBuilder.class);
	
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			GiftVoucherPaymentOption paymentOptionGV= new GiftVoucherPaymentOption(paymentOption);
			return paymentOptionGV;
			
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Gift Voucher Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode)
			throws Exception {
		logger.info("Getting GV Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Net Banking is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_GV);
		logger.info("Is GV enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if GV enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_GV);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common GV option bean exists. Returning null..");
			return null;
		}

		// Build GV PaymentOption bean for given merchant and product.
		GiftVoucherPaymentOption gvPaymentOption = new GiftVoucherPaymentOption(commonPaymentOption);
		gvPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning GV Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		return gvPaymentOption;
	}

}
