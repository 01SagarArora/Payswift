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
import com.yatra.payment.ui.dao.bean.UINoCostEmiBanks;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.EmiType;
import com.yatra.payment.ui.display.beans.NoCostEmiBank;
import com.yatra.payment.ui.display.beans.NoCostEmiProduct;
import com.yatra.payment.ui.util.PaymentUIUtil;
import java.util.ArrayList;

@Service("noCostCacheBuilder")
public class NoCostEMICacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(NoCostEMICacheBuilder.class);

	@Autowired
	private PaymentUIGenericDAO<UINoCostEmiBanks> noCostEmiDAO;
	@Autowired
	private UICacheManager<NoCostEmiProduct> noCostEmiBanksCache;
	@Autowired
	private BeanConvertor<UINoCostEmiBanks, NoCostEmiProduct> noCostEmiBankConvertor;

	@Override
	public void build() {
		try {
			List<UINoCostEmiBanks> noCostBanks = noCostEmiDAO.getAll();
			if (noCostBanks != null) {
				for (UINoCostEmiBanks noCostBank : noCostBanks) {
                                        NoCostEmiProduct banks = noCostEmiBankConvertor.convert(noCostBank);
                                        if (banks == null) {
                                            continue;
                                        }
                                        List<String> products = noCostBank.getProducts();
                                        for (String product : products) {
//                                            if (noCostEmiBanksCache.get(product) == null) {
                                            noCostEmiBanksCache.set(product, banks);
//                                            } else {
//                                                NoCostEmiProduct products1 = noCostEmiBanksCache.get(product);
//                                                List<NoCostEmiBank> banks1 = products1.getEmiBanks();
//                                                banks1.add(banks);
//                                            }
                                        }
				}
				
			}
			logger.info("No Cost EMI Bank Cache build successfully. Total " + noCostBanks.size()+ " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating NoCostEMIBankCache. Payment UI will not be rendered.", ex);
		}
	}
        
        public void reload() throws Exception {
            List<UINoCostEmiBanks> noCostBanks = noCostEmiDAO.getAll();
            if (noCostBanks != null) {
                    for (UINoCostEmiBanks noCostBank : noCostBanks) {
                            NoCostEmiProduct banks = noCostEmiBankConvertor.convert(noCostBank);
                            if (banks == null) {
                                continue;
                            }
                            List<String> products = noCostBank.getProducts();
                            for (String product : products) {
//                                            if (noCostEmiBanksCache.get(product) == null) {
                                noCostEmiBanksCache.set(product, banks);
//                                            } else {
//                                                NoCostEmiProduct products1 = noCostEmiBanksCache.get(product);
//                                                List<NoCostEmiBank> banks1 = products1.getEmiBanks();
//                                                banks1.add(banks);
//                                            }
                            }
                    }

            }
        }
}
