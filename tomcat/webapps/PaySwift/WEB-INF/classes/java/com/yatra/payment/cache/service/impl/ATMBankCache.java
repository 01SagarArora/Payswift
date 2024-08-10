package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.Bank;

@Service("atmBankCache")
public class ATMBankCache implements UICacheManager<Bank> {

	private Map<String, Bank> atmBanksMap = new ConcurrentHashMap<String, Bank>();
	
	@Override
	public Bank get(String key) {
		return atmBanksMap.get(key);
	}

	@Override
	public List<Bank> getAll() {
		return new ArrayList<Bank>(atmBanksMap.values());
	}

	@Override
	public List<Bank> getAll(String key) {
		return null;
	}

	@Override
	public void set(String key, Bank value) {
		atmBanksMap.put(key, value);
	}

	@Override
	public void remove(String key) {
		atmBanksMap.remove(key);
	}

	@Override
	public void removeAll() {
		atmBanksMap.clear();
	}

	@Override
	public void setAll(String key, List<Bank> value) {
	}

}
