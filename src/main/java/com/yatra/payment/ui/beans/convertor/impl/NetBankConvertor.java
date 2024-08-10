package com.yatra.payment.ui.beans.convertor.impl;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UINbBank;
import com.yatra.payment.ui.display.beans.Bank;
import com.yatra.payment.ui.enums.Status;

@Service("netBankConvertor")
public class NetBankConvertor implements BeanConvertor<UINbBank, Bank>{

	@Override
	public Bank convert(UINbBank uiNbBank) {
		Bank bank = new Bank();
		bank.setCode(uiNbBank.getBankCode());
		bank.setDisplayText(uiNbBank.getDisplayName());
		bank.setLogoURL(uiNbBank.getImageURL());
		bank.setPreferred(uiNbBank.getPreferred());
		bank.setStatus(Status.valueOf(uiNbBank.getStatus()));
		bank.setIframeEnabled(uiNbBank.getIframeEnabled());
		bank.setPriority(uiNbBank.getPriority());
		return bank;
	}

}
