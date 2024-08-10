package com.yatra.payment.ui.cache.builder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIEmiBanks;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.EmiType;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("emiBankCacheBuilder")
public class EMIBankCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(EMIBankCacheBuilder.class);

	@Autowired
	private PaymentUIGenericDAO<UIEmiBanks> emiBanksDAO;
	@Autowired
	private UICacheManager<EMIBank> emiBanksCache;
	@Autowired
	private BeanConvertor<UIEmiBanks, EMIBank> emiBankConvertor;

	@Override
	public void build() {
		try {
			List<UIEmiBanks> emiBanks = emiBanksDAO.getAll();
			if (emiBanks != null) {
				for (UIEmiBanks emiBank : emiBanks) {
					EMIBank bank = emiBankConvertor.convert(emiBank);
					if (PaymentUIUtil.isEnabled(bank.getStatus())) {
						EMIBank cacheBank = emiBanksCache.get(bank.getCode());
						if (cacheBank != null) {
							List<EmiType> emiTypes = bank.getEmiTypes();
							List<EmiType> cacheEmiTypes = cacheBank.getEmiTypes();
							cacheEmiTypes.addAll(emiTypes);
							bank.setEmiTypes(cacheEmiTypes);
						}
						emiBanksCache.set(bank.getCode(), bank);
					}
				}
				List<EMIBank> emiBanksFromCache = emiBanksCache.getAll();
				for (EMIBank emiBank : emiBanksFromCache) {
					List<EmiType> emiTypesPrevBank = emiBank.getEmiTypes();
					Collections.sort(emiTypesPrevBank, new Comparator<EmiType>() {
						public int compare(EmiType emiType1, EmiType emiType2) {
							int tenure1 = Integer.parseInt(emiType1.getEmiTenure());
							int tenure2 = Integer.parseInt(emiType2.getEmiTenure());
							if (tenure1 > tenure2)
								return 1;
							return -1;
						}
					});
				}
			}
			logger.info("EMI Bank Cache build successfully. Total " + emiBanks.size() + " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating EMIBankCache. Payment UI will not be rendered.", ex);
		}
	}

}
