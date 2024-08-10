package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIPayopCardTypesMapping;

@Service("payopCardTypesMappingCacheBuilder")
public class PayopCardTypesMappingCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(PayopCardTypesMappingCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UIPayopCardTypesMapping> payopCardTypesMappingDao;
	@Autowired	private UICacheManager<UIPayopCardTypesMapping> payopCardTypesMappingCache;

	public void build() {
		try {
			payopCardTypesMappingCache.removeAll();
			List<UIPayopCardTypesMapping> payopCardTypesMapping = payopCardTypesMappingDao.getAll();
			if (payopCardTypesMapping != null && !payopCardTypesMapping.isEmpty()) {
				
				for (UIPayopCardTypesMapping cardType : payopCardTypesMapping) {

					// Check if any cardType has been added to cache
					List<UIPayopCardTypesMapping> payopCardTypeList = payopCardTypesMappingCache.getAll(cardType.getPaymentOption());
					
					// Not present. Initialize new arraylist
					if (payopCardTypeList == null) 
						payopCardTypeList = new ArrayList<UIPayopCardTypesMapping>();
					
					// Add cardType in list
					payopCardTypeList.add(cardType);
					payopCardTypesMappingCache.setAll(cardType.getPaymentOption(), payopCardTypeList);
				}
			}
			logger.info("PayopCardTypesMappingCache build successfully. Total " + payopCardTypesMapping.size() + " records ");
			
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating PayopCardTypesMappingCache. Payment UI will not be rendered.", ex);
		}
	}
}