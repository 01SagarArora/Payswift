package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.NoCostEmiProduct;

@Service("noCostEmiBanksCache")
public class NoCostEMIBanksCache implements UICacheManager<NoCostEmiProduct> {

	Map<String, NoCostEmiProduct> emiBankMap = new ConcurrentHashMap<String, NoCostEmiProduct>(); 
	
	@Override
	public NoCostEmiProduct get(String key) {
		return emiBankMap.get(key);
	}

	@Override
	public List<NoCostEmiProduct> getAll() {
		return new ArrayList<NoCostEmiProduct>(emiBankMap.values());
	}

	@Override
	public List<NoCostEmiProduct> getAll(String key) {
		return null;
	}

	@Override
	public void set(String key, NoCostEmiProduct value) {
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
	public void setAll(String key, List<NoCostEmiProduct> value) {
	}

}
