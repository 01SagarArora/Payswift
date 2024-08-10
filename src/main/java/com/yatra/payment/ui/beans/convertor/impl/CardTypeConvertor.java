package com.yatra.payment.ui.beans.convertor.impl;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UICardTypes;
import com.yatra.payment.ui.display.beans.CardType;

@Service("cardTypeConvertor")
public class CardTypeConvertor implements BeanConvertor<UICardTypes, CardType> {

	@Override
	public CardType convert(UICardTypes uiCardType) {
		CardType cardType = new CardType();
		cardType.setCode(uiCardType.getCode());
		cardType.setLogoURL(uiCardType.getImageURL());
		return cardType;
	}
}
