package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;

@Service("productServiceUrlCache")
public class ProductServiceUrlCache implements UICacheManager<ProductServiceUrlBean>{

	
	Map<String, ProductServiceUrlBean> lobUrlMap = new ConcurrentHashMap<String, ProductServiceUrlBean>(); 
	
	@Override
	public ProductServiceUrlBean get(String key) {
		return lobUrlMap.get(key);
	}

	@Override
	public List<ProductServiceUrlBean> getAll() {
		return new ArrayList<ProductServiceUrlBean>(lobUrlMap.values());
	}

	@Override
	public List<ProductServiceUrlBean> getAll(String key) {
		return null;
	}

	@Override
	public void set(String key, ProductServiceUrlBean lobServiceUrlBean) {
		lobUrlMap.put(key, lobServiceUrlBean);
	}

	@Override
	public void remove(String key) {
		lobUrlMap.remove(key);
		
	}

	@Override
	public void removeAll() {
		lobUrlMap.clear();
	}

	@Override
	public void setAll(String key, List<ProductServiceUrlBean> value) {
		
	}

}
