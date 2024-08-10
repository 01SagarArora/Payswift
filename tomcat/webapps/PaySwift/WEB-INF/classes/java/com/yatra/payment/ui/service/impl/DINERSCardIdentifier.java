package com.yatra.payment.ui.service.impl;

import org.springframework.stereotype.Component;
import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Component("dinersCardIdentifier")
public class DINERSCardIdentifier implements CardTypeIdentifier {

	@Override
	public String getCardType(String bin) {
		
		if(bin.length() == 9 ) {
			if(bin.matches("^3(?:0[0-5]|[68][0-9])[0-9]{6}$")) {
				return PaymentUIUtil.CARD_TYPE_DINERS;
			}
		}
		if(bin.matches("^3(?:0[0-5]|[68][0-9])[0-9]{3}$")) {
			return PaymentUIUtil.CARD_TYPE_DINERS;
		}
		return null;

	}

}
