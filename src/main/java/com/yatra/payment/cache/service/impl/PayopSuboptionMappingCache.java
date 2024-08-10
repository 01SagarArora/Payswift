package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;

@Service("payopSuboptionMappingCache")
public class PayopSuboptionMappingCache implements UICacheManager<List<UIPayopSuboptionMapping>> {

	private Map<String, List<UIPayopSuboptionMapping>> payopSuboptionMap = new ConcurrentHashMap<String, List<UIPayopSuboptionMapping>>();

	@Override
	public List<UIPayopSuboptionMapping> get(String key) {
		return payopSuboptionMap.get(key);
	}

	@Override
	public List<List<UIPayopSuboptionMapping>> getAll() {
		return null;
	}

	@Override
	public List<List<UIPayopSuboptionMapping>> getAll(String key) {
		return null;
	}

	@Override
	public void set(String key, List<UIPayopSuboptionMapping> value) {
		payopSuboptionMap.put(key, value);
	}

	@Override
	public void remove(String key) {
		payopSuboptionMap.remove(key);
	}

	@Override
	public void removeAll() {
		payopSuboptionMap.clear();
	}

	@Override
	public void setAll(String key, List<List<UIPayopSuboptionMapping>> value) {
		
	}
}
