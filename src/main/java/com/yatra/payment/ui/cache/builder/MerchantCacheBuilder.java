package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.display.beans.Merchant;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("merchantCacheBuilder")
public class MerchantCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(MerchantCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<Merchant> merchantMasterDao;
	@Autowired	private UICacheManager<Merchant> merchantCache;

	public void build() {
		try {
			List<Merchant> merchants = merchantMasterDao.getAll();
			if (merchants != null && !merchants.isEmpty()) {
				for (Merchant merchant : merchants) {
					String key = PaymentUIUtil.getPaymentDivKey(merchant.getMerchantCode(), merchant.getProductCode());
					merchantCache.set(key, merchant);
				}
			}
			logger.info("MerchantCache build successfully. Total " + merchants.size() + " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating MerchantCache. Payment UI will not be rendered.", ex);
		}
	}
}
