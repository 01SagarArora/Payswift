package com.yatra.payment.ui.payswift.service;

import com.google.gson.Gson;
import com.paytm.pg.merchant.PaytmChecksum;
import com.yatra.payment.payswift.beans.PaytmResponseBean;
import com.yatra.payment.payswift.beans.SessionStorageCardInfo;
import com.yatra.payment.ui.dao.SessionDao;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.util.PaytmTokenizationUtil;
import com.yatra.platform.util.YatraUtil;
import com.yatra.tokenized.cards.v3.beans.UserCardInfo;
import com.yatra.tokenized.cards.v3.beans.UserCardsDisplayResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.TreeMap;


@Service
public class CardsService {
	
	@Autowired SessionDao sessionDao;
	@Autowired private PaymentUIHelper paymentUIHelper;
	@Autowired
	private PayswiftStagesDAOImpl payswiftStagesDAO;

	private Gson gson = new Gson();

	Logger logger = Logger.getLogger(TemporarilySaveCardWithCVVProcessor.class);

	public boolean saveCardInSessionDB(String userId, SessionStorageCardInfo sessionCardInfo) {
		if(sessionCardInfo == null)
			return false;
		else return sessionDao.insertToSessionMaster(userId, encryptCardDetails(userId, sessionCardInfo));
	}

	public SessionStorageCardInfo getCardFromSessionDB(String userId) {
		return decryptCardDetails(userId, sessionDao.getCardInfoFromSessionDB(userId));
	}

	private SessionStorageCardInfo encryptCardDetails(String userId, 
			SessionStorageCardInfo sessionCardInfo) {
		AESEncryptionProviderServiceImpl encryptionService = new AESEncryptionProviderServiceImpl();
		try {
			String keyPhrase = encryptionService.getKeyPhrase(userId);
			String cardNo = sessionCardInfo.getCardNumber();
			if(!YatraUtil.isNullOrEmpty(cardNo))
				sessionCardInfo.setCardNumber(encryptionService.getEncryptedData(cardNo, keyPhrase));
			
			String month = sessionCardInfo.getExpiryMonth();
			if(!YatraUtil.isNullOrEmpty(month))
				sessionCardInfo.setExpiryMonth(encryptionService.getEncryptedData(month, keyPhrase));
			
			String year = sessionCardInfo.getExpiryYear();
			if(!YatraUtil.isNullOrEmpty(year))
				sessionCardInfo.setExpiryYear(encryptionService.getEncryptedData(year, keyPhrase));
			
			String cvv = sessionCardInfo.getCvv();
			if(!YatraUtil.isNullOrEmpty(cvv))
				sessionCardInfo.setCvv(encryptionService.getEncryptedData(cvv, keyPhrase));
			
			
		} catch (Exception e) {
			logger.error("Exception while encrypting session details :" + e.getMessage());
		}

		return sessionCardInfo;
	}
	
	
	private SessionStorageCardInfo decryptCardDetails(String userId, 
			SessionStorageCardInfo sessionCardInfo) {
		AESEncryptionProviderServiceImpl encryptionService = new AESEncryptionProviderServiceImpl();
		try {
			String keyPhrase = encryptionService.getKeyPhrase(userId);
			String cardNo = sessionCardInfo.getCardNumber();
			if(!YatraUtil.isNullOrEmpty(cardNo))
				sessionCardInfo.setCardNumber(encryptionService.getDecryptionKey(cardNo, keyPhrase));
			
			String month = sessionCardInfo.getExpiryMonth();
			if(!YatraUtil.isNullOrEmpty(month))
				sessionCardInfo.setExpiryMonth(encryptionService.getDecryptionKey(month, keyPhrase));
			
			String year = sessionCardInfo.getExpiryYear();
			if(!YatraUtil.isNullOrEmpty(year))
				sessionCardInfo.setExpiryYear(encryptionService.getDecryptionKey(year, keyPhrase));
			
			String cvv = sessionCardInfo.getCvv();
			if(!YatraUtil.isNullOrEmpty(cvv))
				sessionCardInfo.setCvv(encryptionService.getDecryptionKey(cvv, keyPhrase));
			
			
		} catch (Exception e) {
			logger.error("Exception while encrypting session details :" + e.getMessage());
		}

		return sessionCardInfo;
	}

	public PaytmResponseBean deleteTokenisedCard(String cardId, String userId){

		String response = "";
		try {

			String custId  = new String(Base64.getEncoder().encode(("YATRA_1_" + userId).getBytes()));

			JSONObject paytmParams = new JSONObject();

			TreeMap<String, String> body = new TreeMap<String, String>();
			body.put("mid", PaytmTokenizationUtil.MID_VALUE);
			body.put("custId", custId);
			body.put("cardId", cardId);
			body.put("requestedBy", "CARDHOLDER");
			body.put("reason", "Customer wants to delete the token");
			body.put("reasonCode", "CUSTOMER_CONFIRMED");
			body.put("isParRequest", "false");
			body.put("isBulkDelete", "false");
			String checksum = PaytmChecksum.generateSignature(body, PaytmTokenizationUtil.MERCHANT_KEY);

			JSONObject head = new JSONObject();
			head.put("token",checksum);
			head.put("tokenType", "CHECKSUM");
			paytmParams.put("body", body);
			paytmParams.put("head", head);

			String url = PaytmTokenizationUtil.getTokenizeCardRemoveUrl(payswiftStagesDAO) + "?mid=" + PaytmTokenizationUtil.MID_VALUE + "&custId=" + custId;
			paymentUIHelper.logIntoDB(paytmParams.toString(), "REQUEST", "--", url);
			response = PaytmTokenizationUtil.getResponse(paytmParams.toString(), url);

			paymentUIHelper.logIntoDB(response, "RESPONSE", "--", url);



		} catch (Exception e) {
			logger.error("Error in fetching Paytm API response" , e);
		}
		if(StringUtils.isNotBlank(response)) {
			return gson.fromJson(response, PaytmResponseBean.class);
		}else{
			return null;
		}

	}


	public String getPaytmReferenceId(UserCardsDisplayResponse cardsForDisplayOfGuestUser, String cardId) {
		if(StringUtils.isNotBlank(cardId) && (cardsForDisplayOfGuestUser != null) && (CollectionUtils.isNotEmpty(cardsForDisplayOfGuestUser.getUserCards()))){
			String res = "";
			List<UserCardInfo> userCardInfos = cardsForDisplayOfGuestUser.getUserCards();
			for(UserCardInfo userCardInfo : userCardInfos){
				if(userCardInfo.getCardId().equalsIgnoreCase(cardId)){
					res = userCardInfo.getTokenRefID();
				}
			}
			return res;
		}
		return null;
	}
}
