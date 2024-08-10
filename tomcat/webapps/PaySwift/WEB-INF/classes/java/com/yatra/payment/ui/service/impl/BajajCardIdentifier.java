package com.yatra.payment.ui.service.impl;

import org.springframework.stereotype.Component;

import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Component("bajajCardIdentifier")
public class BajajCardIdentifier implements CardTypeIdentifier{

	@Override
	public String getCardType(String bin) {
		
		if(bin.length() == 9) {
			if(bin.startsWith("203040")) {
				return PaymentUIUtil.CARD_TYPE_BAJAJ;
			}
		}
		if ("203040".equals(bin)) {
			return PaymentUIUtil.CARD_TYPE_BAJAJ;
		}
		return null;
	}

}
