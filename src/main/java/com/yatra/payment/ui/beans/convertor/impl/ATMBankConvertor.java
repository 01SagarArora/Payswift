package com.yatra.payment.ui.beans.convertor.impl;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIAtmBanks;
import com.yatra.payment.ui.display.beans.Bank;
import com.yatra.payment.ui.enums.Status;

@Service("atmBankConvertor")
public class ATMBankConvertor implements BeanConvertor<UIAtmBanks, Bank> {

	@Override
	public Bank convert(UIAtmBanks input) {
		Bank bank = new Bank();
		bank.setCode(input.getBankCode());
		bank.setDisplayText(input.getDisplayName());
		bank.setLogoURL(input.getImageURL());
		bank.setStatus(Status.valueOf(input.getStatus()));
		return bank;
	}

}
