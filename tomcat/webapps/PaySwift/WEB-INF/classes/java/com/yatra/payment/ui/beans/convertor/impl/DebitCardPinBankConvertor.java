package com.yatra.payment.ui.beans.convertor.impl;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIDcPinBank;
import com.yatra.payment.ui.display.beans.Bank;
import com.yatra.payment.ui.enums.Status;
import org.springframework.stereotype.Service;

@Service("dcPinBankConvertor")
public class DebitCardPinBankConvertor implements BeanConvertor<UIDcPinBank, Bank> {

    @Override
    public Bank convert(UIDcPinBank uiDcPinBank) {
        Bank bank = new Bank();
        bank.setCode(uiDcPinBank.getBankCode());
        bank.setDisplayText(uiDcPinBank.getDisplayName());
        bank.setLogoURL(uiDcPinBank.getImageURL());
        bank.setPreferred(uiDcPinBank.getPreferred());
        bank.setStatus(Status.valueOf(uiDcPinBank.getStatus()));
        return bank;
    }
}
