package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UICardTypesParamMapping;

@Service("cardTypeParamMappingCache")
public class CardTypeParamMappingCache implements UICacheManager<UICardTypesParamMapping> {

	Map<String, UICardTypesParamMapping> cardTypeParamMap = new ConcurrentHashMap<String, UICardTypesParamMapping>();

	@Override
	public UICardTypesParamMapping get(String key) {
		return cardTypeParamMap.get(key);
	}

	@Override
	public List<UICardTypesParamMapping> getAll() {
		return new ArrayList<UICardTypesParamMapping>(cardTypeParamMap.values());
	}

	@Override
	public void set(String key, UICardTypesParamMapping value) {
		cardTypeParamMap.put(key, value);
	}

	@Override
	public void remove(String key) {
		cardTypeParamMap.remove(key);
	}

	@Override
	public void removeAll() {
		cardTypeParamMap.clear();
	}

	@Override
	public List<UICardTypesParamMapping> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<UICardTypesParamMapping> value) {
		// TODO Auto-generated method stub
		
	}

}
