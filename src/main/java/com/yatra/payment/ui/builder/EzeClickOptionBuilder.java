package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopSubTypesMappingCache;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.EzeClickPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("ezeClickOptionBuilder")
public class EzeClickOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(EzeClickOptionBuilder.class);

	@Autowired private PayopSubTypesMappingCache payopSubTypesMappingCache;
	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			EzeClickPaymentOption commonPaymentOption = new EzeClickPaymentOption(paymentOption);
			UIPayopSubTypesMapping payopSubTypesMapping = payopSubTypesMappingCache.get(paymentOption.getCode());
			if (payopSubTypesMapping != null) {
				commonPaymentOption.setDisplayName(payopSubTypesMapping.getDisplayName());
				commonPaymentOption.setOptionCode(payopSubTypesMapping.getOptionCode());
				commonPaymentOption.setLogoURL(payopSubTypesMapping.getImageURL());
			}
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building EzeClickOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting ezeClick Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if ezeClick is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_EZE_CLICK);
		logger.info("Is ezeClick enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no ezeClick entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if ezeClick is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get ezeClick PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_EZE_CLICK);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common ezeClick payment option bean exists. Returning null..");
			return null;
		}
		
		// Build ezeClick PaymentOption bean for given merchant and product.
		EzeClickPaymentOption ezeClickPaymentOption = new EzeClickPaymentOption(commonPaymentOption);
		ezeClickPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning ezeClick Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return ezeClickPaymentOption;
	}

}
