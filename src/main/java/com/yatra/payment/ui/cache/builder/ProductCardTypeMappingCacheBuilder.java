package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIProductCardTypeMapping;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("productCardTypeMappingCacheBuilder")
public class ProductCardTypeMappingCacheBuilder implements CacheBuilder {
	
	private static Logger logger = Logger.getLogger(ProductCardTypeMappingCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UIProductCardTypeMapping> productCardTypeMappingDao;
	@Autowired	private UICacheManager<UIProductCardTypeMapping> productCardTypeMappingCache;

	@Override
	public void build() {
		try {
			productCardTypeMappingCache.removeAll();
			List<UIProductCardTypeMapping> productCardTypeMappings = productCardTypeMappingDao.getAll();
			if (productCardTypeMappings != null) {
				
				for (UIProductCardTypeMapping productCardTypeMapping : productCardTypeMappings) {
					String key = PaymentUIUtil.getPaymentDivKey(productCardTypeMapping.getMerchantCode(), productCardTypeMapping.getProductCode());
					
					// Check if any cardType has been added to cache
					List<UIProductCardTypeMapping> productCardTypeList = productCardTypeMappingCache.getAll(key);
					
					// Not present. Initialize new arraylist
					if (productCardTypeList == null) 
						productCardTypeList = new ArrayList<UIProductCardTypeMapping>();
					
					// Add cardType in list
					productCardTypeList.add(productCardTypeMapping);
					productCardTypeMappingCache.setAll(key, productCardTypeList);
				}
			}
			logger.info("ProductCardTypeMappingCache build successfully. Total " + productCardTypeMappings.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating ProductCardTypeMappingCache. Payment UI will not be rendered.", ex);
		}
	}

}
