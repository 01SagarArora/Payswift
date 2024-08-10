package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.Bank;

@Service("netBankCache")
public class NetBankCache implements UICacheManager<Bank> {

	private Map<String, Bank> banks = new ConcurrentHashMap<String, Bank>();

	@Override
	public Bank get(String key) {
		return banks.get(key);
	}

	@Override
	public void set(String key, Bank bank) {
		banks.put(key, bank);
	}

	@Override
	public void remove(String key) {
		banks.remove(key);
	}

	@Override
	public void removeAll() {
		banks.clear();
	}

	@Override
	public List<Bank> getAll() {
		return new ArrayList<Bank>(banks.values());
	}

	@Override
	public List<Bank> getAll(String key) {
		return null;
	}

	@Override
	public void setAll(String key, List<Bank> value) {
	}

}
