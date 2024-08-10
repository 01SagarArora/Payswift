package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UICardTypesParamMappingCentral;

@Service("cardTypeParamMappingCentralCache")
public class CardTypeParamMappingCentralCache implements UICacheManager<UICardTypesParamMappingCentral> {

	Map<String, UICardTypesParamMappingCentral> cardTypeParamCentralMap = new ConcurrentHashMap<String, UICardTypesParamMappingCentral>();

	@Override
	public UICardTypesParamMappingCentral get(String key) {
		return cardTypeParamCentralMap.get(key);
	}

	@Override
	public List<UICardTypesParamMappingCentral> getAll() {
		return new ArrayList<UICardTypesParamMappingCentral>(cardTypeParamCentralMap.values());
	}

	@Override
	public void set(String key, UICardTypesParamMappingCentral value) {
		cardTypeParamCentralMap.put(key, value);
	}

	@Override
	public void remove(String key) {
		cardTypeParamCentralMap.remove(key);
	}

	@Override
	public void removeAll() {
		cardTypeParamCentralMap.clear();
	}

	@Override
	public List<UICardTypesParamMappingCentral> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<UICardTypesParamMappingCentral> value) {
		// TODO Auto-generated method stub
		
	}

}
