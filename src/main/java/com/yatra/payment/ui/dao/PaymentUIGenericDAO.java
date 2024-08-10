package com.yatra.payment.ui.dao;

import java.util.List;

public interface PaymentUIGenericDAO<T> {

	List<T> getAll() throws Exception;
	List<T> getList(T t) throws Exception;
	T get(T t) throws Exception;
}
