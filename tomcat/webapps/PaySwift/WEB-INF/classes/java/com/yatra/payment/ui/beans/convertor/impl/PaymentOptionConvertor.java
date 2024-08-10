package com.yatra.payment.ui.beans.convertor.impl;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIPayop;
import com.yatra.payment.ui.display.beans.PaymentOption;

@Service("paymentOptionConvertor")
public class PaymentOptionConvertor implements BeanConvertor<UIPayop, PaymentOption>{

	@Override
	public PaymentOption convert(UIPayop uiPayop) {
		PaymentOption paymentOption = new PaymentOption();
		paymentOption.setCode(uiPayop.getCode());
		paymentOption.setPriority(uiPayop.getPriority());
		paymentOption.setDisplayText(uiPayop.getDisplayName());
		paymentOption.setIframeEnabled(uiPayop.getIframeEnabled());
		return paymentOption;
	}
}
