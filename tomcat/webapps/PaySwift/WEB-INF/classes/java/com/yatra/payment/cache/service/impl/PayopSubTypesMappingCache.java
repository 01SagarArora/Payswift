package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;

@Service("payopSubTypesMappingCache")
public class PayopSubTypesMappingCache implements UICacheManager<UIPayopSubTypesMapping> {

	private Map<String, UIPayopSubTypesMapping> payopSubTypeMap = new ConcurrentHashMap<String, UIPayopSubTypesMapping>();

	@Override
	public UIPayopSubTypesMapping get(String key) {
		return payopSubTypeMap.get(key);
	}

	@Override
	public List<UIPayopSubTypesMapping> getAll() {
		return null;
	}

	@Override
	public List<UIPayopSubTypesMapping> getAll(String key) {
		return null;
	}

	@Override
	public void set(String key, UIPayopSubTypesMapping value) {
		payopSubTypeMap.put(key, value);
	}

	@Override
	public void remove(String key) {
		payopSubTypeMap.remove(key);
	}

	@Override
	public void removeAll() {
		payopSubTypeMap.clear();
	}

	@Override
	public void setAll(String key, List<UIPayopSubTypesMapping> value) {
	}

}
