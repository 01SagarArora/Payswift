package com.yatra.payment.ui.cache.builder;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIDcPinBank;
import com.yatra.payment.ui.display.beans.Bank;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("debitCardPinBankCacheBuilder")
public class DebitCardPinBankCacheBuilder implements CacheBuilder{

    private static Logger logger = Logger.getLogger(NetBankCacheBuilder.class);

    @Autowired
    private PaymentUIGenericDAO<UIDcPinBank> dcPinBanksDAO;
    @Autowired	private UICacheManager<Bank> debitCardPinBankCache;
    @Autowired	private BeanConvertor<UIDcPinBank, Bank> dcPinBankConvertor;

    @Override
    public void build() {
        try {
            List<UIDcPinBank> uiDcPinBanks = dcPinBanksDAO.getAll();
            if (uiDcPinBanks != null) {
                for (UIDcPinBank uiDcPinBank : uiDcPinBanks) {
                    Bank bank = dcPinBankConvertor.convert(uiDcPinBank);
                    debitCardPinBankCache.set(bank.getCode(), bank);
                }
            }
            logger.info("Debit Card Pin Bank Cache build successfully. Total " + uiDcPinBanks.size() + " message mapping ");
        } catch (Exception ex) {
            logger.error("SEVERE!! Error occurred while creating DcPinBankCache. Payment UI will not be rendered.", ex);
        }
    }
}
