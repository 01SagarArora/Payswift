package com.yatra.payment.ui.beans.convertor.impl;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.CashCard;
import com.yatra.payment.ui.enums.Status;

@Service("cashCardConvertor")
public class CashCardConvertor implements BeanConvertor<UIPayopSubTypesMapping, CashCard> {

	@Override
	public CashCard convert(UIPayopSubTypesMapping input) {
		CashCard cashCard = new CashCard();
		cashCard.setCode(input.getOptionCode());
		cashCard.setDisplayText(input.getDisplayName());
		cashCard.setLogoURL(input.getImageURL());
		cashCard.setStatus(Status.valueOf(input.getStatus()));
		return cashCard;
	}

}
