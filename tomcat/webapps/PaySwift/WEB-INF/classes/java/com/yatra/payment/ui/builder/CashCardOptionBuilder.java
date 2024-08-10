package com.yatra.payment.ui.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.display.beans.CashCard;
import com.yatra.payment.ui.display.beans.CashCardPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("cashCardOptionBuilder")
public class CashCardOptionBuilder implements PaymentOptionBuilder {

	private static Logger logger = Logger.getLogger(CashCardOptionBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UIPayopSubTypesMapping> payopSubTypesMappingDAO;
	@Autowired	private BeanConvertor<UIPayopSubTypesMapping, CashCard> cashCardConvertor;
	@Autowired	private UICacheManager<PaymentOption> paymentOptionCache;
	@Autowired protected StatusProviderService statusProviderService;

	@Override
	public PaymentOption build(PaymentOption paymentOption) throws Exception {
		try {
			CashCardPaymentOption commonPaymentOption = new CashCardPaymentOption(paymentOption);
			setCashCards(paymentOption.getCode(), commonPaymentOption);
			return commonPaymentOption;
		} catch (Exception e) {
			logger.error("SEVERE!! Error occurred while building CashCardOption ", e);
			throw e;
		}
	}

	@Override
	public PaymentOption build(String merchantCode, String productCode) throws Exception {
		
		logger.info("Getting Cash Card Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		
		// Check if Cash Card is enabled for given merchant and product. 
		Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_CASH_CARD);
		logger.info("Is Cash Card enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);
		
		// If isPayOpEnabled is null, this implies that no Cash Card entry exists for given merchant and product, so return null.
		if(isPayOpEnabled == null) {
			logger.info("Could not be determined if Cash Card is enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
			return null;
		}
		// Get Cash Card PaymentOption bean which is common for all merchants and products.
		PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_CASH_CARD);
		
		// If commonPaymentOption = null, return null.
		if(commonPaymentOption == null) {
			logger.info("No common Cash Card payment option bean exists. Returning null..");
			return null;
		}
		
		// Build Cash Card PaymentOption bean for given merchant and product.
		CashCardPaymentOption cashCardPaymentOption = new CashCardPaymentOption(commonPaymentOption);
		isPayOpEnabled = getUpdatedStatus(cashCardPaymentOption.getCashCards());
		cashCardPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
		logger.info("Returning Cash Card Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
		return cashCardPaymentOption;
	}
	
	private void setCashCards(String paymentOption, CashCardPaymentOption cashCardPaymentOption) {
		UIPayopSubTypesMapping payopSubTypesMapping = new UIPayopSubTypesMapping();
		payopSubTypesMapping.setPaymentOption(paymentOption);
		List<CashCard> cashCards = new ArrayList<CashCard>();
		try {
			List<UIPayopSubTypesMapping> cashCardList = payopSubTypesMappingDAO.getList(payopSubTypesMapping);
			if (cashCardList != null && !cashCardList.isEmpty()) {
				for (UIPayopSubTypesMapping mapping : cashCardList) {
					cashCards.add(cashCardConvertor.convert(mapping));
				}
			}
		} catch (Exception e) {
			logger.error("Exception encountered during setCashCards for payment option " + paymentOption + e);
		}
		cashCardPaymentOption.setCashCards(cashCards);
	}
	
	private boolean getUpdatedStatus(List<CashCard> cashCards) {
		// Iterate over all cash cards. If no cash card is enabled, return false. If even one of the cash cards is enabled, return true.
		boolean isCashCardEnabled = false;
		if (cashCards != null && !cashCards.isEmpty()) {
			for (CashCard cashCard : cashCards) {
				if (PaymentUIUtil.isEnabled(cashCard.getStatus())) {
					isCashCardEnabled = true;
					break;
				}
			}
		}
		return isCashCardEnabled;
	}
}
