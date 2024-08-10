package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.impl.QuickBookCardTypeCache;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIQBCardTypeMaster;

@Service("qbCardTypeCacheBuilder")
public class QBCardTypeCacheBuilder implements CacheBuilder {
	
	private static Logger logger = Logger.getLogger(QBCardTypeCacheBuilder.class);
	
	@Autowired private PaymentUIGenericDAO<UIQBCardTypeMaster> qbCardTypeDao;
	@Autowired private QuickBookCardTypeCache quickBookCardTypeCache;
	
	@Override
	public void build() {
		try {
			List<UIQBCardTypeMaster> qbCardTypeList = qbCardTypeDao.getAll();
			if(qbCardTypeList != null && !qbCardTypeList.isEmpty()) {
				for(UIQBCardTypeMaster qbCardType : qbCardTypeList) {
					quickBookCardTypeCache.set(qbCardType.getCode(), qbCardType);
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred while building QB Card Type cache ", e);
		}
	}
}
