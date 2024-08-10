package com.yatra.payment.cache.service;

import java.util.List;
import java.util.Map;

public interface UICacheManager<T> {

	T get(String key);
	List<T> getAll();
	List<T> getAll(String key);
	void set(String key, T value);
    void remove(String key);
    void removeAll();
    void setAll(String key, List<T> value);
    default void setCache(Map<String,List<T>> cacheMap){}
    default Map<String,List<T>> getCache(){ return null;}
    
}
