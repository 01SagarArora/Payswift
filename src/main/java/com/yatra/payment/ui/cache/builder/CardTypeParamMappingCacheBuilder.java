package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UICardTypesParamMapping;

@Service("cardTypeParamMappingCacheBuilder")
public class CardTypeParamMappingCacheBuilder implements CacheBuilder {
	private static Logger logger = Logger.getLogger(CardTypeParamMappingCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UICardTypesParamMapping> cardTypeParamMappingDao;
	@Autowired	private UICacheManager<UICardTypesParamMapping> cardTypeParamMappingCache;

	public void build() {
		try {
			List<UICardTypesParamMapping> mappings = cardTypeParamMappingDao.getAll();
			if (mappings != null) {
				for (UICardTypesParamMapping mapping : mappings) {
					String key = mapping.getPaymentOption() + "_" + mapping.getCardType() + "_" + mapping.getParamKey();
					cardTypeParamMappingCache.set(key, mapping);
				}
			}
			logger.info("CardTypeParamMappingCache build successfully. Total " + mappings.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating CardTypeParamMappingCache. Payment UI will not be rendered.", ex);
		}
	}

}
