package com.yatra.payment.ui.service.impl;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.yatra.payment.ui.service.CardTypeIdentifier;

public class MainCardTypeIdentifier implements  CardTypeIdentifier {
	@Autowired
	private List<CardTypeIdentifier> cardIdentifiers;

	public String getCardType(String bin) {
		if(StringUtils.isBlank(bin)){
			return null;
		}
		for(CardTypeIdentifier cardTypeIdentifier : cardIdentifiers){
			String cardType = cardTypeIdentifier.getCardType(bin);
			if(StringUtils.isNotBlank(cardType)){
				return cardType;
			}
		}
		return "unknown";
	}
	
	public void setCardIdentifiers(List<CardTypeIdentifier> cardIdentifiers) {
		this.cardIdentifiers = cardIdentifiers;
	}
}

