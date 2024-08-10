package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.builder.PaymentOptionBuilder;
import com.yatra.payment.ui.builder.PaymentOptionBuilderFactory;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIPayop;
import com.yatra.payment.ui.display.beans.PaymentOption;

@Service("paymentOptionCacheBuilder")
public class PaymentOptionCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(PaymentOptionCacheBuilder.class);

	@Autowired private PaymentUIGenericDAO<UIPayop> paymentOptionDao;
	@Autowired private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired private BeanConvertor<UIPayop, PaymentOption> paymentOptionConvertor;
	@Autowired private PaymentOptionBuilderFactory paymentOptionBuilderFactory;

	@Override
	public void build() {
		try {
			// Get list of all possible payment options.
			List<UIPayop> uiPayOptions = paymentOptionDao.getAll();
			if (uiPayOptions != null && !uiPayOptions.isEmpty()) {
				
				// For each payment option construct a PaymentOption bean (commonPaymentOption) which will be common for all merchants and products.
				for (UIPayop uiPayOption : uiPayOptions) {
					try {
						// Convert UiPayOp to PaymentOption bean.
						PaymentOption paymentOption = paymentOptionConvertor.convert(uiPayOption);
						
						// Get PaymentOptionBuilder corresponding to the payment option, that will be used to build commonPaymentOption.
						PaymentOptionBuilder paymentOptionBuilder = paymentOptionBuilderFactory.createPaymentOptionBuilder(paymentOption.getCode());
						if(paymentOptionBuilder != null) {
							
							// Build commonPaymentOption (common for all merchants and products).
							PaymentOption commonPaymentOption = paymentOptionBuilder.build(paymentOption);
							
							// Set this commonPaymentOption bean in cache, with key = payment option code.
							paymentOptionCache.set(paymentOption.getCode(), commonPaymentOption);							
						} else {
							logger.error("No Payment Option builder exists for payment option retrieved from ui_payop table : " + uiPayOption.getCode());
						}
						
					} catch (Exception e) {
						logger.error("SEVERE!! Error occurred while building a payment option. Skiping this payment option : " + uiPayOption.getCode() + " ", e);
						continue;
					}
				}
			}
			logger.info("PaymentOptionCache build successfully. Total " + uiPayOptions.size() + " payment options.");
			
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating PaymentOptionCache. Payment UI will not be rendered.", ex);
		}
	}
}
