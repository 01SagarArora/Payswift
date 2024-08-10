package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIMerchantPayopMapping;

@Service("merchantPayOpCache")
public class MerchantPayOpCache implements UICacheManager<UIMerchantPayopMapping> {

	private Map<String, List<UIMerchantPayopMapping>> merchantPayopListMap = new ConcurrentHashMap<String, List<UIMerchantPayopMapping>>();

	@Override
	public UIMerchantPayopMapping get(String key) {
		return null;
	}

	@Override
	public void set(String key, UIMerchantPayopMapping value) {
	}

	@Override
	public void remove(String key) {
		merchantPayopListMap.remove(key);
	}

	@Override
	public void removeAll() {
		merchantPayopListMap.clear();
	}

	@Override
	public List<UIMerchantPayopMapping> getAll() {
		return null;
	}

	@Override
	public List<UIMerchantPayopMapping> getAll(String key) {
		return merchantPayopListMap.get(key);
	}

	@Override
	public void setAll(String key, List<UIMerchantPayopMapping> value) {
		merchantPayopListMap.put(key, value);
	}

}
