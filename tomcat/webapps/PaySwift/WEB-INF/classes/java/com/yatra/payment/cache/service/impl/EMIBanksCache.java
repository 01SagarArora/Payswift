package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.EMIBank;

@Service("emiBanksCache")
public class EMIBanksCache implements UICacheManager<EMIBank> {

	Map<String, EMIBank> emiBankMap = new ConcurrentHashMap<String, EMIBank>(); 
	
	@Override
	public EMIBank get(String key) {
		return emiBankMap.get(key);
	}

	@Override
	public List<EMIBank> getAll() {
		return new ArrayList<EMIBank>(emiBankMap.values());
	}

	@Override
	public List<EMIBank> getAll(String key) {
		return null;
	}

	@Override
	public void set(String key, EMIBank value) {
		emiBankMap.put(key, value);
	}

	@Override
	public void remove(String key) {
		emiBankMap.remove(key);
	}

	@Override
	public void removeAll() {
		emiBankMap.clear();
	}

	@Override
	public void setAll(String key, List<EMIBank> value) {
	}

}
