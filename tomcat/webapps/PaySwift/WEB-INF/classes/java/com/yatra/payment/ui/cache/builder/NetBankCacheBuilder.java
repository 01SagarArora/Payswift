package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UINbBank;
import com.yatra.payment.ui.display.beans.Bank;

@Service("netBankCacheBuilder")
public class NetBankCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(NetBankCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UINbBank> netBanksDao;
	@Autowired	private UICacheManager<Bank> netBankCache;
	@Autowired	private BeanConvertor<UINbBank, Bank> netBankConvertor;

	public void build() {
		try {
			List<UINbBank> uiNetBanks = netBanksDao.getAll();
			if (uiNetBanks != null) {
				for (UINbBank uiNetBank : uiNetBanks) {
					Bank bank = netBankConvertor.convert(uiNetBank);
					netBankCache.set(bank.getCode(), bank);
				}
			}
			logger.info("Net Bank Cache build successfully. Total " + uiNetBanks.size() + " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating NetBankCache. Payment UI will not be rendered.", ex);
		}
	}
}