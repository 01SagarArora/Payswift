package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.bean.UIQBCardTypeMaster;

@Service("quickBookCardTypeCache")
public class QuickBookCardTypeCache implements UICacheManager<UIQBCardTypeMaster> {

	private Map<String, UIQBCardTypeMaster> uiQBCardTypeMaster = new ConcurrentHashMap<String, UIQBCardTypeMaster>();

	@Override
	public UIQBCardTypeMaster get(String key) {
		return uiQBCardTypeMaster.get(key);
	}

	@Override
	public List<UIQBCardTypeMaster> getAll() {
		return new ArrayList<UIQBCardTypeMaster>(uiQBCardTypeMaster.values());
	}

	@Override
	public void set(String key, UIQBCardTypeMaster value) {
		uiQBCardTypeMaster.put(key, value);
	}

	@Override
	public void remove(String key) {
		uiQBCardTypeMaster.remove(key);
	}

	@Override
	public void removeAll() {
		uiQBCardTypeMaster.clear();
	}

	@Override
	public List<UIQBCardTypeMaster> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<UIQBCardTypeMaster> value) {
		// TODO Auto-generated method stub
		
	}

}
