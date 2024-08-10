package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.Merchant;

/**
 * Key : merchant_product
 * Value : Merchant properties for merchant_product
 */

@Service("merchantCache")
public class MerchantCache implements UICacheManager<Merchant> {

	private Map<String, Merchant> merchants = new ConcurrentHashMap<String, Merchant>();

	@Override
	public Merchant get(String key) {
		return merchants.get(key);
	}

	@Override
	public void set(String key, Merchant merchant) {
		merchants.put(key, merchant);
	}

	@Override
	public void remove(String key) {
		merchants.remove(key);
	}

	@Override
	public void removeAll() {
		merchants.clear();
	}

	@Override
	public List<Merchant> getAll() {
		return new ArrayList<Merchant>(merchants.values());
	}

	@Override
	public List<Merchant> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<Merchant> value) {
		// TODO Auto-generated method stub

	}

}
