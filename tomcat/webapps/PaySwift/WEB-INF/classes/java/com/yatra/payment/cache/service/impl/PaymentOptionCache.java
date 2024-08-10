package com.yatra.payment.cache.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.PaymentOption;

@Service("paymentOptionCache")
public class PaymentOptionCache implements UICacheManager<PaymentOption> {

	private Map<String, PaymentOption> paymentOptions = new ConcurrentHashMap<String, PaymentOption>();
	
	@Override
	public PaymentOption get(String key) {
		return paymentOptions.get(key);
	}

	@Override
	public void set(String key, PaymentOption paymentOptionsBean) {
		paymentOptions.put(key, paymentOptionsBean);
	}

	@Override
	public void remove(String key) {
		paymentOptions.remove(key);
	}

	@Override
	public void removeAll() {
		paymentOptions.clear();
	}

	@Override
	public List<PaymentOption> getAll() {
		return new ArrayList<PaymentOption>(paymentOptions.values());
	}

	@Override
	public List<PaymentOption> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<PaymentOption> value) {
		// TODO Auto-generated method stub
		
	}

}
