package com.yatra.payment.ui.beans.convertor.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIEmiBanks;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.EmiType;
import com.yatra.payment.ui.enums.Status;

@Service("emiBankConvertor")
public class EMIBankConvertor implements BeanConvertor<UIEmiBanks, EMIBank> {

	@Override
	public EMIBank convert(UIEmiBanks input) {
		EMIBank bank = new EMIBank();
		bank.setCode(input.getCode());
		bank.setDisplayText(input.getDisplayName());
		bank.setLogoURL(input.getImageUrl());
		bank.setStatus(Status.valueOf(input.getStatus()));
		bank.setTransactionFee(input.getTransactionFee());
		bank.setMinAmount(input.getMinAmount());
		bank.setMaxAmount(input.getMaxAmount());
		bank.setDisplayMobile(input.getDisplayMobile());

		List<CardType> cardTypeList = new ArrayList<CardType>();
		if (input.getCardTypes() != null && input.getCardTypes().size() > 0) {
			for (String card : input.getCardTypes()) {
				CardType cardType = new CardType();
				cardType.setCode(card);
				cardTypeList.add(cardType);
			}
		}
		bank.setCardTypes(cardTypeList);
		
		EmiType emiType = new EmiType(input.getTenure(), input.getInterestRate(), input.getMinAmount(), input.getMaxAmount());
		List<EmiType> emiTypes = new ArrayList<EmiType>();
		emiTypes.add(emiType);
		bank.setEmiTypes(emiTypes);
		return bank;
	}

}
