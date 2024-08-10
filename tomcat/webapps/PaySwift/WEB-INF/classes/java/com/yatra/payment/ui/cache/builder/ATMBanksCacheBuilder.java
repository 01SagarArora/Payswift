package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIAtmBanks;
import com.yatra.payment.ui.display.beans.Bank;

@Service("atmBankCacheBuilder")
public class ATMBanksCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(CardTypeCacheBuilder.class);

	@Autowired	private UICacheManager<Bank> atmBankCache;
	@Autowired	private PaymentUIGenericDAO<UIAtmBanks> atmBanksDAO;
	@Autowired	private BeanConvertor<UIAtmBanks, Bank> atmBankConvertor;
	
	@Override
	public void build() {
		try {
			List<UIAtmBanks> uiAtmBanks = atmBanksDAO.getAll();
			if (uiAtmBanks != null) {
				for (UIAtmBanks atmBank : uiAtmBanks) {
					Bank bank = atmBankConvertor.convert(atmBank);
					atmBankCache.set(atmBank.getBankCode(), bank);
				}
			}
			logger.info("ATM Bank Cache build successfully. Total " + uiAtmBanks.size() + " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating ATMBankCache. Payment UI will not be rendered.", ex);
		}

	}

}
