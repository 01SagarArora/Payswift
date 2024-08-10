package com.yatra.payment.ui.service.impl;

import org.springframework.stereotype.Component;
import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Component("amexCardIdentifier")
public class AmexCardIdentifier implements CardTypeIdentifier {

	@Override
	public String getCardType(String bin) {
		
		if(bin.length() == 9) {
			if(bin.matches("(^3[47][0-9]{7})$")) {
				return PaymentUIUtil.CARD_TYPE_AMEX;
			}
		}
		
		if(bin.matches("^(^3[47][0-9]{4})$")) {
			return PaymentUIUtil.CARD_TYPE_AMEX;
		}
		return null;
	}
	
	

}
