package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIMerchantPayopMapping;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("merchantPayopCacheBuilder")
public class MerchantPayopCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(MerchantPayopCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UIMerchantPayopMapping> merchantPayopMappingDao;
	@Autowired	private UICacheManager<UIMerchantPayopMapping> merchantPayOpCache;

	public void build() {
		try {
			// Get payment options for all merchants.
			List<UIMerchantPayopMapping> merchantPayOps = merchantPayopMappingDao.getAll();
			if (merchantPayOps != null && !merchantPayOps.isEmpty()) {
				
				// For each merchant, create a list of payment options and add to cache (key = merchant_product, value = List of payment options).
				for (UIMerchantPayopMapping merchantPayopMapping : merchantPayOps) {
					String key = PaymentUIUtil.getPaymentDivKey(merchantPayopMapping.getMerchantCode(), merchantPayopMapping.getProductCode());
					
					// Check if ANY payop has been added to cache
					List<UIMerchantPayopMapping> merchantPayopList = merchantPayOpCache.getAll(key);

					// Not present. Initialize new arraylist
					if (merchantPayopList == null)
						merchantPayopList = new ArrayList<UIMerchantPayopMapping>();
					
					merchantPayopList.add(merchantPayopMapping);
					merchantPayOpCache.setAll(key, merchantPayopList);
				}
			}
			logger.info(" Merchant Payment Option Cache build. Total " + merchantPayOps.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating MerchantPayopCache. Payment UI will not be rendered.", ex);
		}
	}
}