package com.yatra.payment.ui.builder;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.*;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("netBankingOptionBuilder")
public class NetBankingOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(NetBankingOptionBuilder.class);

	@Autowired	private UICacheManager<Bank> netBankCache;
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			NetBankingPaymentOption commonPaymentOption = new NetBankingPaymentOption(paymentOption);
			return commonPaymentOption;

		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building Net Banking Option ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {

		logger.info("Getting Net Banking Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

		// Check if Net Banking is enabled for given merchant and product.
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_NET_BANKING);
		logger.info("Is Net Banking enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

		// If isPayOpEnabled is null, this implies that no Net Banking entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Net Banking is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Net Banking PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_NET_BANKING);

		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Net Banking payment option bean exists. Returning null..");
			return null;
		}

		// Build Net Banking PaymentOption bean for given merchant and product.
		NetBankingPaymentOption netBankingPaymentOption = new NetBankingPaymentOption(commonPaymentOption);
		List<Bank> bankList = getEnabledBanks(merchantCode, productCode);
        List<Bank> preferredBankList = getPreferredBankList(bankList);
        netBankingPaymentOption.setBanks(bankList);
        netBankingPaymentOption.setPreferredBanks(preferredBankList);
        netBankingPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Net Banking Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return netBankingPaymentOption;
	}

	private List<Bank> getPreferredBankList(List<Bank> bankList) {
		List<Bank> preferredBanks = new ArrayList<Bank>();
		for(Bank bank: bankList) {
			if (bank.getPreferred() != null && Boolean.TRUE.equals(bank.getPreferred())) {
				preferredBanks.add(bank);
			}
		}
		PreferredBankComparator preferredBankComparator = new PreferredBankComparator();
		Collections.sort(preferredBanks, preferredBankComparator);
		return preferredBanks;
	}

	private List<Bank> getEnabledBanks(String merchantCode, String productCode) throws Exception {

		List<Bank> productBankList = new ArrayList<Bank>();
		try {
			// Get list of all banks.
			List<Bank> bankList = netBankCache.getAll();
			if(bankList == null || bankList.isEmpty()) {
				logger.error("No banks obtained from NetBankCache. Returning an empty list.");
				return productBankList;
			}
			// Iterate over all banks.
			for(Bank bank : bankList) {
				String bankCode = bank.getCode();

				// Determine if bank is enabled.
				boolean isBankEnabled = PaymentUIUtil.isEnabled(bank.getStatus());
				logger.info("Is bank : " + bankCode + " enabled : " + isBankEnabled);

				// If bank is enabled, get corresponding Bank bean for merchant and product.
				if(isBankEnabled) {

					// Determine if bank is enabled for given merchant and product.
					Boolean isBankEnabledForMerchant = statusProviderService.isBankEnabled(merchantCode, productCode, bankCode);
					logger.info("Is bank : " + bankCode + " enabled for merchant :  " + merchantCode + " and product : " + productCode + " : " + isBankEnabledForMerchant);

					// If bank is enabled for given merchant and product, create Bank bean and add it to list.
					if(isBankEnabledForMerchant != null && isBankEnabledForMerchant) {
						productBankList.add(bank);
					}
				}
			}
			// Sort list and return.
			BankComparator bankComparator = new BankComparator();
			Collections.sort(productBankList, bankComparator);
			return productBankList;

		} catch (Exception e) {
			logger.info("Exception ocurred while fetching enabled Banks for merchant : " + merchantCode + " and product : " + productCode, e);
			throw e;
		}
	}
}
