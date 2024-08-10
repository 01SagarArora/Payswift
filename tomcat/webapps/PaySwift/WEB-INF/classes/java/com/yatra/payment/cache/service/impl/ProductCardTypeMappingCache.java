package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIProductCardTypeMapping;

@Service("productCardTypeMappingCache")
public class ProductCardTypeMappingCache implements UICacheManager<UIProductCardTypeMapping> {

	private Map<String, List<UIProductCardTypeMapping>> productCardTypeMappingMap = new ConcurrentHashMap<String, List<UIProductCardTypeMapping>>();

	@Override
	public UIProductCardTypeMapping get(String key) {
		return null;
	}

	@Override
	public List<UIProductCardTypeMapping> getAll() {
		return null;
	}

	@Override
	public List<UIProductCardTypeMapping> getAll(String key) {
		return productCardTypeMappingMap.get(key);
	}

	@Override
	public void set(String key, UIProductCardTypeMapping value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void remove(String key) {
		productCardTypeMappingMap.remove(key);
	}

	@Override
	public void removeAll() {
		productCardTypeMappingMap.clear();
	}

	@Override
	public void setAll(String key, List<UIProductCardTypeMapping> value) {
		productCardTypeMappingMap.put(key, value);
	}

}
