package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.UIMessage;

@Service("payopMessageMappingCache")
public class PayopMessageMappingCache implements UICacheManager<UIMessage> {

	private Map<String, List<UIMessage>> messageMappingMap = new ConcurrentHashMap<String, List<UIMessage>>();

	@Override
	public UIMessage get(String key) {
		return null;
	}

	@Override
	public List<UIMessage> getAll() {
		return null;
	}

	@Override
	public List<UIMessage> getAll(String key) {
		return messageMappingMap.get(key);
	}

	@Override
	public void set(String key, UIMessage value) {
	}

	@Override
	public void remove(String key) {
		messageMappingMap.remove(key);
	}

	@Override
	public void removeAll() {
		messageMappingMap.clear();
	}

	@Override
	public void setAll(String key, List<UIMessage> value) {
		messageMappingMap.put(key, value);
	}
	
	@Override
	public void setCache(Map<String, List<UIMessage>> cacheMap) {
		messageMappingMap = cacheMap;
	}
	
	@Override
	public Map<String, List<UIMessage>> getCache() {
		return messageMappingMap;
	}

}
