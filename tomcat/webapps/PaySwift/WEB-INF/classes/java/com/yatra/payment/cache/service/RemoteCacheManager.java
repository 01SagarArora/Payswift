package com.yatra.payment.cache.service;

public interface RemoteCacheManager {

	public void removeDiv(String merchantCode, String productCode, String cacheName);
    public void removeAllDiv( String cacheName);
}
