package com.yatra.payment.ui.beans.convertor;

public interface BeanConvertor<I, T> {
	T convert(I input);
}
