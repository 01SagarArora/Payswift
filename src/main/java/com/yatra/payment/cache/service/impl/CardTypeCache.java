package com.yatra.payment.cache.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.CardType;

@Service("cardTypeCache")
public class CardTypeCache implements UICacheManager<CardType> {

	private Map<String, CardType> cardTypes = new ConcurrentHashMap<String, CardType>();

	@Override
	public CardType get(String key) {
		return cardTypes.get(key);
	}

	@Override
	public void set(String key, CardType paymentOptionsBean) {
		cardTypes.put(key, paymentOptionsBean);
	}

	@Override
	public void remove(String key) {
		cardTypes.remove(key);
	}

	@Override
	public void removeAll() {
		cardTypes.clear();
	}

	@Override
	public List<CardType> getAll() {
		return (List<CardType>) cardTypes.values();
	}

	@Override
	public List<CardType> getAll(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAll(String key, List<CardType> value) {
		// TODO Auto-generated method stub
		
	}

}
