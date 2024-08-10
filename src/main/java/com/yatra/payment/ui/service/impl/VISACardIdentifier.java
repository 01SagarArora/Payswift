package com.yatra.payment.ui.service.impl;

import org.springframework.stereotype.Component;
import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Component("visaCardIdentifier")
public class VISACardIdentifier implements CardTypeIdentifier {

	@Override
	public String getCardType(String bin) {
		
		
		//For bin number -> 9 
		if(bin.length() == 9) {
			if(bin.matches("^(4[0-9]{8})?")) {
				return PaymentUIUtil.CARD_TYPE_VISA;
			}
		}
		
		
		//For bin number -> 6
		if (bin.matches("^(4[0-9]{5})?")) {
			return PaymentUIUtil.CARD_TYPE_VISA;
		}
		
		
		return null;
	}
}