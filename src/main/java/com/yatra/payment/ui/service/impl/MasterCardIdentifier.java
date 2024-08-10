package com.yatra.payment.ui.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Component("masterCardIdentifier")
public class MasterCardIdentifier implements CardTypeIdentifier {
	
//	private static List<String> binList = Arrays.asList("222708", "222709", "222710","222940","222941");
	

	@Override
	public String getCardType(String bin) {
		final String binRegex = "^(5[1-5][0-9]{7}|2(22[1-9][0-9]{5}|2[3-9][0-9]{6}|[3-6][0-9]{7}|7[0-1][0-9]{6}|720[0-9]{5}))$";
		
		if(bin.length() == 9 ) {
			//Only checking here for the number starting with 5 digit number.
			if(bin.matches(binRegex)) {
				return PaymentUIUtil.CARD_TYPE_MASTER;
			}
		}
		
		if( bin.matches(binRegex)) {
			return PaymentUIUtil.CARD_TYPE_MASTER;
		}
		return null;
	}

}
