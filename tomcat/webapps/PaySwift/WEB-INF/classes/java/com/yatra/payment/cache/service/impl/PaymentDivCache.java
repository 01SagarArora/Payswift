package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentDiv;

@Service("paymentDivCache")
public class PaymentDivCache implements UICacheManager<PaymentDiv> {

	private Map<String, PaymentDiv> paymentDivMap = new ConcurrentHashMap<String, PaymentDiv>();
	
	public PaymentDiv get(String key) {
		return paymentDivMap.get(key);
	}

	public void set(String key, PaymentDiv paymentOptionsBean) {
		paymentDivMap.put(key, paymentOptionsBean);
	}

	public void remove(String key) {
		paymentDivMap.remove(key);
	}

	public void removeAll() {
		paymentDivMap.clear();
	}

	@Override
	public List<PaymentDiv> getAll() {
		return new ArrayList<PaymentDiv>(paymentDivMap.values());
	}

	@Override
	public List<PaymentDiv> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<PaymentDiv> value) {
		// TODO Auto-generated method stub
		
	}

}
