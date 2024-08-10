package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIProductBanksMapping;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("productBanksCacheBuilder")
public class ProductBanksCacheBuilder implements CacheBuilder {
	private static Logger logger = Logger.getLogger(ProductBanksCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UIProductBanksMapping> productBanksMappingDao;
	@Autowired	private UICacheManager<UIProductBanksMapping> productBanksCache;
	
	@Override
	public void build() {
		try {
			List<UIProductBanksMapping> mappings = productBanksMappingDao.getAll();
			if (mappings != null) {
				for (UIProductBanksMapping mapping : mappings) {
					String key = PaymentUIUtil.getPaymentDivKey(mapping.getMerchantCode(), mapping.getProductCode());
					
					List<UIProductBanksMapping> productBanks = productBanksCache.getAll(key);
					
					// Not present. Initialize new arraylist
					if (productBanks == null) 
						productBanks = new ArrayList<UIProductBanksMapping>();
					
					// Add cardType in list
					productBanks.add(mapping);
					productBanksCache.setAll(key, productBanks);
				}
			}
			logger.info("ProductBanksCache build successfully. Total " + mappings.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating ProductBanksCache. Payment UI will not be rendered.", ex);
		}
	}

}
