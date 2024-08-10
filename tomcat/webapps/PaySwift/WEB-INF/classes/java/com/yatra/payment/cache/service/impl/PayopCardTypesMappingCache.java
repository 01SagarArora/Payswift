package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIPayopCardTypesMapping;

@Service("payopCardTypesMappingCache")
public class PayopCardTypesMappingCache implements UICacheManager<UIPayopCardTypesMapping> {

	private Map<String, List<UIPayopCardTypesMapping>> payopCrdTypeMapping = new ConcurrentHashMap<String, List<UIPayopCardTypesMapping>>();

	@Override
	public UIPayopCardTypesMapping get(String key) {
		return null;
	}

	@Override
	public void set(String key, UIPayopCardTypesMapping value) {
	}

	@Override
	public void remove(String key) {
		payopCrdTypeMapping.remove(key);
	}

	@Override
	public void removeAll() {
		payopCrdTypeMapping.clear();
	}

	@Override
	public List<UIPayopCardTypesMapping> getAll() {
		return null;
	}

	@Override
	public List<UIPayopCardTypesMapping> getAll(String key) {
		return payopCrdTypeMapping.get(key);
	}

	@Override
	public void setAll(String key, List<UIPayopCardTypesMapping> value) {
		payopCrdTypeMapping.put(key, value);
	}

}
