package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.cards.v3.beans.UserCardInfo;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.payswift.beans.SessionStorageCardInfo;
import com.yatra.payment.ui.dao.SessionDao;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.QuickBookSaveCardService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;

@Service
public class TemporarilySaveCardWithCVVProcessor implements PayNowProcessor {

	@Autowired
	private PayNowProcessor walletAuthProcessor;
	@Autowired
	private CardsService cardService;

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		Logger logger = Logger.getLogger(TemporarilySaveCardWithCVVProcessor.class);
			boolean saveCardInSession = false;
			String product = payNowRequest.getBookingDetails().getProductCode();
			if("INT_HOTEL_BKG".equalsIgnoreCase(product)
					|| "minthoteliosbkg".equalsIgnoreCase(product)
					|| "minthotelandroidbkg".equalsIgnoreCase(product)
					|| "smepinthotelandroidbkg".equalsIgnoreCase(product)
					|| "smeointhotelandroidbkg".equalsIgnoreCase(product)
					|| "smepinthoteliosbkg".equalsIgnoreCase(product)
					|| "smeointhoteliosbkg".equalsIgnoreCase(product))
				saveCardInSession = true;
			
			if(("crpdomhotel".equalsIgnoreCase(product)
					|| "cdomhotelandroid".equalsIgnoreCase(product)
					|| "cdomhotelios".equalsIgnoreCase(product)
					|| "cinthotelandroid".equalsIgnoreCase(product)
					|| "cinthotelios".equalsIgnoreCase(product)
					|| "crpinthotel".equalsIgnoreCase(product))
					&& payNowRequest.getBookingDetails().isBookingDotComFlow())
				saveCardInSession = true;
			
			if(saveCardInSession) {
				logger.debug("Entering saveCardWithinSession flow ");
				boolean successfullySaved = false;
				boolean saveCardWithCVV = payNowRequest.getBookingDetails().saveCardWithCVV();
				String userId = PaymentUIUtil.getUniqueRandomNumberOfFixedLength(36);
				if(payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD) || 
					payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD)) {
					Map<String, String> requestMap = createRequestMap(payNowRequest);
					requestMap.put(PaymentUIUtil.TEMPORARY_USER_ID, userId);
					SessionStorageCardInfo userCardInfo = PaymentUIUtil.getSessionStorageCardInfo(requestMap, saveCardWithCVV);
					successfullySaved = cardService.saveCardInSessionDB(userId, userCardInfo);
				} else if(payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK)) {
					JSONObject response = PaymentUIUtil.getSavedCardDetails(payNowRequest.getCardId(), payNowRequest.getSSOToken());
					SessionStorageCardInfo userCardInfo = PaymentUIUtil.getSessionStorageCardInfo(response, payNowRequest.getPaymentOptionParamMap().get(PaymentUIUtil.CARD_SECURITY_CODE) , userId, saveCardWithCVV);
					successfullySaved = cardService.saveCardInSessionDB(userId, userCardInfo);
				}
				if(successfullySaved) {
					payNowRequest.setUserId(userId);
					logger.info("Card saved successfully in session, against UserId: " + userId);
				}
			}
			return walletAuthProcessor.process(payNowRequest, payNowResponse);
		
	}

	private Map<String, String> createRequestMap(PayNowRequest payNowRequest) {
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put(PaymentUIUtil.MERCHANT_CODE, payNowRequest.getMerchantCode());
		requestMap.put(PaymentUIUtil.PRODUCT_CODE, payNowRequest.getProductCode());
		requestMap.putAll(payNowRequest.getPaymentOptionParamMap());
		requestMap.put(PaymentUIUtil.CARD_TYPE, payNowRequest.getPaymentOption());
		requestMap.put(QuickBookUIUtil.CARD_BRAND, payNowRequest.getCardType());
		return requestMap;
	}

}
