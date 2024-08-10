package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIProductBanksMapping;

@Service("productBanksCache")
public class ProductBanksCache implements UICacheManager<UIProductBanksMapping> {

	private Map<String, List<UIProductBanksMapping>> productBankMap = new ConcurrentHashMap<String, List<UIProductBanksMapping>>();

	
	@Override
	public UIProductBanksMapping get(String key) {
		return null;
	}

	@Override
	public List<UIProductBanksMapping> getAll() {
		return null;
	}

	@Override
	public List<UIProductBanksMapping> getAll(String key) {
		return productBankMap.get(key);
	}

	@Override
	public void set(String key, UIProductBanksMapping value) {
		
	}

	@Override
	public void remove(String key) {
		productBankMap.remove(key);
	}

	@Override
	public void removeAll() {
		productBankMap.clear();
	}

	@Override
	public void setAll(String key, List<UIProductBanksMapping> value) {
		productBankMap.put(key, value);
	}

}
