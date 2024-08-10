package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIQBCardBrandMaster;

@Service("quickBookCardBrandCache")
public class QuickBookCardBrandCache implements UICacheManager<UIQBCardBrandMaster> {

	private Map<String, UIQBCardBrandMaster> uiQBCardBrandMaster = new ConcurrentHashMap<String, UIQBCardBrandMaster>();

	@Override
	public UIQBCardBrandMaster get(String key) {
		return uiQBCardBrandMaster.get(key);
	}

	@Override
	public List<UIQBCardBrandMaster> getAll() {
		return new ArrayList<UIQBCardBrandMaster>(uiQBCardBrandMaster.values());
	}

	@Override
	public void set(String key, UIQBCardBrandMaster value) {
		uiQBCardBrandMaster.put(key, value);
	}

	@Override
	public void remove(String key) {
		uiQBCardBrandMaster.remove(key);
	}

	@Override
	public void removeAll() {
		uiQBCardBrandMaster.clear();
	}

	@Override
	public List<UIQBCardBrandMaster> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<UIQBCardBrandMaster> value) {
		// TODO Auto-generated method stub
		
	}

}
