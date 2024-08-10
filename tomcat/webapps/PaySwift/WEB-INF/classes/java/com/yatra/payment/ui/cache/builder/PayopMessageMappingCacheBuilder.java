package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PayopMessageMappingCache;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIPayopMessageMapping;
import com.yatra.payment.ui.display.beans.UIMessage;
import com.yatra.payment.ui.enums.Status;

@Service("payopMessageMappingCacheBuilder")
public class PayopMessageMappingCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(PayopMessageMappingCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UIPayopMessageMapping> payopMessageMappingDao;
	@Autowired	private UICacheManager<UIMessage> payopMessageMappingCache;
	@Autowired	private BeanConvertor<UIPayopMessageMapping, UIMessage> messageMappingConvertor;

	public void build() {
		try {
			List<UIPayopMessageMapping> payopMessageMapping = payopMessageMappingDao.getAll();
			
			if (payopMessageMapping != null) {
				
				UIMessage uiMessage;
				Map<String, List<UIMessage>> messageMappingMap = new ConcurrentHashMap<String, List<UIMessage>>();
				for (UIPayopMessageMapping message : payopMessageMapping) {
					logger.info("****"+message.getMessageType()+"****");
					uiMessage = messageMappingConvertor.convert(message);

					// Check if any message has been added to cache
					 List<UIMessage> payopMessageMappingList = messageMappingMap.get(message.getPaymentOption());
					
					// Not present. Initialize new arraylist
					if (payopMessageMappingList == null)
						payopMessageMappingList = new ArrayList<UIMessage>();

					// Add messages in payoplist
					if(uiMessage.getStatus().compareTo(Status.ENABLED) == 0)
						payopMessageMappingList.add(uiMessage);
					
					// add list to the cache map against the payment option
					messageMappingMap.put(message.getPaymentOption(), payopMessageMappingList);
					
				}
				
				payopMessageMappingCache.setCache(messageMappingMap);
				
			}
			
			logger.info("PayopMessageMappingCache build successfully. Total " + payopMessageMapping.size() + " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating PayopMessageMappingCache. Payment UI will not be rendered.", ex);
		}
	}
}