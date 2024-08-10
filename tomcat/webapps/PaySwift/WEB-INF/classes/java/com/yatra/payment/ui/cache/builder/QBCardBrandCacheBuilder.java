package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.impl.QuickBookCardBrandCache;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIQBCardBrandMaster;

@Service("qbCardBrandCacheBuilder")
public class QBCardBrandCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(QBCardBrandCacheBuilder.class);
	
	@Autowired private PaymentUIGenericDAO<UIQBCardBrandMaster> qbCardBrandDao;
	@Autowired private QuickBookCardBrandCache quickBookCardBrandCache;
	
	@Override
	public void build() {
		try {
			List<UIQBCardBrandMaster> qbCardBrandList = qbCardBrandDao.getAll();
			if(qbCardBrandList != null && !qbCardBrandList.isEmpty()) {
				for(UIQBCardBrandMaster qbCardBrand : qbCardBrandList) {
					quickBookCardBrandCache.set(qbCardBrand.getCode(), qbCardBrand);
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred while building QB Card Brand Cache", e);
		}
		
	}
}
