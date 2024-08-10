package com.yatra.payment.ui.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.bean.Currency;
import com.yatra.payment.ui.dao.bean.UIMerchantCurrencyMapping;
import com.yatra.payment.ui.dao.impl.CurrencyDAOImpl;
import com.yatra.payment.ui.dao.impl.UIMerchantCurrencyMappingDaoImpl;

@Service
public class CurrencyConfigCache {
	
	private static Logger logger = Logger.getLogger(CurrencyConfigCache.class);
	
	@Autowired private UIMerchantCurrencyMappingDaoImpl currencyMapperDao;
	@Autowired private CurrencyDAOImpl currencyDao;
	
	private Map<String, Currency> masterCurrencyMap = new HashMap<>();
	private Map<String, List<UIMerchantCurrencyMapping>> currencyMapperMap = new HashMap<>();
	
	@PostConstruct
	void createCurrencyMap() {
		try {
			setMasterCurrencyMap();
			List<UIMerchantCurrencyMapping> currencyList = currencyMapperDao.getAll();
			if(currencyMapperMap != null || !currencyMapperMap.isEmpty())
				currencyMapperMap.clear();
			for(UIMerchantCurrencyMapping currency : currencyList) {
				if(masterCurrencyMap.containsKey(currency.getCurrencyCode())) {
					if(currencyMapperMap.containsKey(getKey(currency.getMerchantCode(), currency.getProductCode()))){
						currencyMapperMap.get(getKey(currency.getMerchantCode(), currency.getProductCode())).add(currency);
					} else {
						currencyMapperMap.put(getKey(currency.getMerchantCode(), currency.getProductCode()), new ArrayList<>(Arrays.asList(currency)));
					}
				}
			}
			
			for(Map.Entry<String, List<UIMerchantCurrencyMapping>> entry : currencyMapperMap.entrySet()) {
				Collections.sort(entry.getValue());
			}
			
		} catch (Exception ex) {
			logger.error("Error while creating currency map cache, Exception is : ", ex);
		}
	}
	
	private void setMasterCurrencyMap(){
		try {
			List<Currency> masterCurrencyList = currencyDao.getAll();
			if(masterCurrencyList != null && !masterCurrencyList.isEmpty()) {
				for(Currency masterCurrency : masterCurrencyList) {
					masterCurrencyMap.put(masterCurrency.getCode(), masterCurrency);
				}
			}
			
		} catch (Exception ex) {
			logger.error("Error while fetching master currencies from DB, Exception is : ", ex);
		}
	}
	
	public List<Currency> getMasterCurrencyList() {
		List<Currency> masterCurrencyList = new ArrayList<>();
		try {
			if(masterCurrencyMap != null && !masterCurrencyMap.isEmpty()) {
				for(String key : masterCurrencyMap.keySet()) {
					masterCurrencyList.add(masterCurrencyMap.get(key));
				}
			} else {
				logger.debug("SEVERE:: MasterCurrencyMap is empty, fetching currencies from DB.");
				masterCurrencyList =  currencyDao.getAll();
			}
			
		} catch (Exception ex) {
			logger.error("Error while getting master currency list, Exception is : " , ex);
		}
		return masterCurrencyList;
	}
	
	public List<UIMerchantCurrencyMapping> getEnabledCurrencies(String merchant, String product) {
		return currencyMapperMap.get(getKey(merchant, product));
	}
	
	public void buildNewCache() {
		createCurrencyMap();
	}
	
	private String getKey(String merchant, String product) {
		return merchant + "_" + product; 
	}
	
}
