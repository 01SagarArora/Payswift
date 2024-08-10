package com.yatra.payment.cache.service.impl;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.RemoteCacheManager;
import com.yatra.platform.payment.cache.ICache;
import com.yatra.platform.payment.cache.impl.RedisCacheImpl;

@Service("remoteCacheManager")
public class RemoteCacheManagerImpl implements RemoteCacheManager {
	private Logger logger = Logger.getLogger(RemoteCacheManagerImpl.class);

	public void removeDiv(String merchantCode, String productCode, String cacheName) {
		try {
			String key = getKey(merchantCode, productCode);
			ICache<String, String> cache = new RedisCacheImpl<String, String>(cacheName);
			cache.remove(key);
		} catch (Exception e) {
			logger.error("Exception occurred while removing Payment Div from cache for merchant_code : " + merchantCode + " and product_code : " + productCode + " : ", e);
		}
	}

	public void removeAllDiv(String cacheName) {
		try {
			ICache<String, String> cache = new RedisCacheImpl<String, String>(cacheName);
			cache.removeAll();
		} catch (Exception e) {
			logger.error("Exception occurred while removing all Payment Divs from cache named : " + cacheName + " : ", e);
		}
	}

	public String getKey(String merchantCode, String productCode) {
		StringBuilder cacheKey = new StringBuilder();
		return cacheKey.append(merchantCode).append("_").append(productCode).append("_").append("payment_div").toString();
	}

}
