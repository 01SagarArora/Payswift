package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;

@Service("payopSuboptionCacheBuilder")
public class PayopSuboptionMappingCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(PayopSuboptionMappingCacheBuilder.class);

	@Autowired	private UICacheManager<List<UIPayopSuboptionMapping>> payopSuboptionMappingCache;
	@Autowired	private PaymentUIGenericDAO<UIPayopSuboptionMapping> payopSuboptionMappingDAO;

	@Override
	public void build() {
		try {
			List<UIPayopSuboptionMapping> payopSuboptionMappings = payopSuboptionMappingDAO.getAll();
			Map<String, List<UIPayopSuboptionMapping>> payopVssuboptionList = new HashMap<String, List<UIPayopSuboptionMapping>>();
			List<UIPayopSuboptionMapping> payopSuboptionList = null;
			if (payopSuboptionMappings != null) {
				for (UIPayopSuboptionMapping subTypesMapping : payopSuboptionMappings) {
					payopSuboptionList = payopVssuboptionList.get(subTypesMapping.getPaymentOption()+"_"+subTypesMapping.getMerchantCode()+"_"+subTypesMapping.getProductCode());
					if(payopSuboptionList==null){
						payopSuboptionList = new ArrayList<UIPayopSuboptionMapping>();
					}
					payopSuboptionList.add(subTypesMapping);
					payopVssuboptionList.put(subTypesMapping.getPaymentOption(), payopSuboptionList);
					payopVssuboptionList.put(subTypesMapping.getPaymentOption()+"_"+subTypesMapping.getMerchantCode()+"_"+subTypesMapping.getProductCode(), payopSuboptionList);
				}
			}
			for (Map.Entry<String, List<UIPayopSuboptionMapping>> entry : payopVssuboptionList.entrySet()) {
				Collections.sort(entry.getValue());
				payopSuboptionMappingCache.set(entry.getKey(), entry.getValue());
			}
			logger.info("PayopSuboptionMapping build successfully. Total " + payopSuboptionMappings.size() + " records");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating PayopSuboptionCache. " , ex);
		}
	}

}