package com.yatra.payment.ui.cache.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.yatra.payment.ui.dao.CompletePaymentInfoDAO;
import com.yatra.payment.ui.display.beans.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.service.PaymentDivGenerator;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("paymentDivCacheBuilder")
public class PaymentDivCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(PaymentDivCacheBuilder.class);

	@Autowired	private UICacheManager<PaymentDiv> paymentDivCache;
	@Autowired	private UICacheManager<Merchant> merchantCache;
	@Autowired private PaymentDivGenerator paymentDivGenerator;
	@Autowired private CompletePaymentInfoDAO completePaymentInfoDAO;
	public void build() {
		try {
			// Get list of all merchants.
			List<Merchant> merchantProductList = merchantCache.getAll();
			
			if(merchantProductList != null && !merchantProductList.isEmpty()) {
				
				// For each merchant and product, generate PaymentDiv and add it to cache.
				for (Merchant merchantProduct : merchantProductList) {
					
					String merchantCode = merchantProduct.getMerchantCode();
					String productCode = merchantProduct.getProductCode();

					// Create Payment Div for merchant and product.
					PaymentDiv paymentDiv = paymentDivGenerator.generatePaymentDiv(merchantCode, productCode);
					//Modify paymentdiv for App/PWA revamp
					updatePaymentOptionsTree(paymentDiv);
					
					// Set payment Div in cache.
					setPaymentDivInCache(merchantCode, productCode, paymentDiv);
				}				
			} else {
				logger.error("Payment Div could not be built. No merchants obtained from MerchantCache");
			}

		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating Payment Div Cache. Payment UI will not be rendered.", ex);
		}
	}
	
	private void setPaymentDivInCache(String merchantCode, String productCode, PaymentDiv paymentDiv) {
		String key = PaymentUIUtil.getPaymentDivKey(merchantCode, productCode);
		paymentDivCache.set(key, paymentDiv);
		logger.info("Added Payment Div Cache for merchantCode : " + merchantCode + " and productCode : " + productCode);
	}

	private void updatePaymentOptionsTree(PaymentDiv paymentDiv)
	{

		ArrayList<PaymentOption> allPaymentOptions = (ArrayList<PaymentOption>) paymentDiv.getPaymentOptions();
		AllUpiPaymentOption allUpiPaymentOption = new AllUpiPaymentOption();
		List<String> upiBankCodes = completePaymentInfoDAO.getUpiBankCodes();
		allUpiPaymentOption.setUpiBankCodes(upiBankCodes);
		AllCardsPaymentOptions allCardsPaymentOptions = new AllCardsPaymentOptions();
		List<PaymentOption> allUpiList = new ArrayList<>();
		allUpiPaymentOption.setDisplayText("Pay using UPI");
		List<PaymentOption> allcardPayopList = new ArrayList<>();
		allCardsPaymentOptions.setDisplayText("Debit & Credit Cards");

		for (int i = 0; i < allPaymentOptions.size(); i++) {
			if (allPaymentOptions.get(i).getCode().equalsIgnoreCase("phonePe") || allPaymentOptions.get(i).getCode().equalsIgnoreCase("paytm") || allPaymentOptions.get(i).getCode().equalsIgnoreCase("tez")) {
				PaymentOption obj = allPaymentOptions.get(i);
				allUpiList.add(obj);
			}

			if (allPaymentOptions.get(i).getCode().equalsIgnoreCase("cc") || allPaymentOptions.get(i).getCode().equalsIgnoreCase("dc") || allPaymentOptions.get(i).getCode().equalsIgnoreCase("qb")) {
				PaymentOption obj = allPaymentOptions.get(i);
				allcardPayopList.add(obj);
			}
		}

		if(!allUpiList.isEmpty()) {
			allUpiPaymentOption.setUpiList(allUpiList);
			allUpiPaymentOption.setCode("allupi");
			allPaymentOptions.add(allUpiPaymentOption);
		}
		if(!allcardPayopList.isEmpty()) {
			allCardsPaymentOptions.setCardList(allcardPayopList);
			allCardsPaymentOptions.setCode("ccdc");
			allPaymentOptions.add(allCardsPaymentOptions);
		}
		paymentDiv.setPaymentOptions(allPaymentOptions);

	}
}