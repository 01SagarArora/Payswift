package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;

@Service("payopSubTypesCacheBuilder")
public class PayopSubTypesMappingCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(PayopSubTypesMappingCacheBuilder.class);

	@Autowired	private UICacheManager<UIPayopSubTypesMapping> payopSubTypesMappingCache;
	@Autowired	private PaymentUIGenericDAO<UIPayopSubTypesMapping> payopSubTypesMappingDAO;

	@Override
	public void build() {
		try {
			List<UIPayopSubTypesMapping> payopSubTypesMappings = payopSubTypesMappingDAO.getAll();
			if (payopSubTypesMappings != null) {
				for (UIPayopSubTypesMapping subTypesMapping : payopSubTypesMappings) {
					payopSubTypesMappingCache.set(subTypesMapping.getPaymentOption(), subTypesMapping);
				}
			}
			logger.info("PayopSubTypesCache build successfully. Total " + payopSubTypesMappings.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating PayopSubTypesCache. " , ex);
		}
	}

}
