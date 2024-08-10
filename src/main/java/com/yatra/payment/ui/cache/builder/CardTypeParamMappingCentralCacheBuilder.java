package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UICardTypesParamMappingCentral;

@Service("cardTypeParamMappingCentralCacheBuilder")
public class CardTypeParamMappingCentralCacheBuilder implements CacheBuilder {
	private static Logger logger = Logger.getLogger(CardTypeParamMappingCentralCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UICardTypesParamMappingCentral> cardTypeParamMappingCentralDao;
	@Autowired	private UICacheManager<UICardTypesParamMappingCentral> cardTypeParamMappingCentralCache;

	public void build() {
		try {
			List<UICardTypesParamMappingCentral> mappingCentrals = cardTypeParamMappingCentralDao.getAll();
			if (mappingCentrals != null) {
				for (UICardTypesParamMappingCentral mappingCentral : mappingCentrals) {
					String key = mappingCentral.getPaymentOption() + "_" + mappingCentral.getCardType() + "_" + mappingCentral.getParamKey();
					cardTypeParamMappingCentralCache.set(key, mappingCentral);
				}
			}
			logger.info("CardTypeParamMappingCentralCache build successfully. Total " + mappingCentrals.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating CardTypeParamMappingCentralCache. Payment UI will not be rendered.", ex);
		}
	}

}
