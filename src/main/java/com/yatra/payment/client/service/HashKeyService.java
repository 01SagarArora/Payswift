package com.yatra.payment.client.service;

import java.security.MessageDigest;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.net.ssl.internal.www.protocol.https.BASE64Encoder;
import com.yatra.payment.client.PaymentClient;
import com.yatra.payment.client.enums.PaymentOption;
import com.yatra.payment.client.utils.PaymentClientUtil;
import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.util.YatraUtil;

@Service("hashKeyService")
public class HashKeyService {
	
	private Logger logger = Logger.getLogger(HashKeyService.class);
	@Autowired private PaymentClient paymentClient;
	@Autowired	private PropertyManager  yatraPropertyReader;

	
	public String getHashKey(String paymentParams, String ttid, String amount, String cvvRequired, String isZeroAmountTxn, String ixigoMoney, PayNowRequest payNowRequest) throws Exception {
		
		if (StringUtils.isBlank(paymentParams) || StringUtils.isBlank(ttid) || StringUtils.isBlank(amount)){
			logger.error("Null / Blank parameter(s) received.");
			throw new Exception("Null / Blank parameter(s) received.");
		}

		// Create paymentParamsMap from paymentParams string
		Map<String, String> paymentParamsMap = paymentClient.getPaymentParamsMap(paymentParams);

		// Get payment option
		PaymentOption paymentOption = PaymentClientUtil.getPaymentOption(paymentParamsMap);

		// Get hash key String for payment option
		String hashKeyString = getHKStringForPayOp(paymentOption, paymentParamsMap, ttid, amount, cvvRequired, isZeroAmountTxn, ixigoMoney,payNowRequest);

		// Generate Hash Key
		String hashKeyPhrase = yatraPropertyReader.getProperty("hash.key.phrase");
		String hashKeyInput = hashKeyString + hashKeyPhrase;
		String hashKey = generateHashKey(hashKeyInput);
		logger.info("Hash key generated is : " + hashKey);
		return hashKey;
	}
	
	public String getResponseHashKey(String paymentStatus, String amount, String merchantTxnRefId) throws Exception {
		
		if (StringUtils.isBlank(paymentStatus) || StringUtils.isBlank(amount) || StringUtils.isBlank(merchantTxnRefId)) {
			logger.error("Hash Key cannot be generated. Null / Blank parameter(s) received.");
			throw new Exception("Hash Key cannot be generated. Null / Blank parameter(s) received.");
		}

		// Generate Hash Key
		String hashKeyString = paymentStatus + amount + merchantTxnRefId;
		String hashKeyPhrase = yatraPropertyReader.getProperty("hash.key.phrase");
		String hashKeyInput = hashKeyString + hashKeyPhrase;
		String hashKey = generateHashKey(hashKeyInput);
		logger.info("Hash key generated is : " + hashKey);
		return hashKey;
	}

	private String getHKStringForPayOp(PaymentOption paymentOption, Map<String, String> paymentParamsMap, String ttid, String amount, String cvvRequired, String isZeroAmountTxn, String ixigoMoney, PayNowRequest payNowRequest) throws Exception {

		try {
			switch (paymentOption) {
				case CC: return getHKStringForCardPayment(paymentParamsMap, ttid, amount, cvvRequired, isZeroAmountTxn, ixigoMoney);
				case DC: return getHKStringForCardPayment(paymentParamsMap, ttid, amount, cvvRequired, isZeroAmountTxn, ixigoMoney);
				case EMI: return getHKStringForCardPayment(paymentParamsMap, ttid, amount, cvvRequired, isZeroAmountTxn, ixigoMoney);
				case REWARDS: return getHKStringForRewardPayment(paymentParamsMap, ttid, amount, ixigoMoney); // Need to discuss
				case QB: return getHKStringForQBPayment(paymentParamsMap, ttid, amount, cvvRequired, isZeroAmountTxn, ixigoMoney);
				case CORPCARD: return getHKStringForCorpPayment(ttid, amount, isZeroAmountTxn);
				case CASHPOOL: return getHKStringForCashPool(ttid,amount,payNowRequest);
				default: return getHKString(ttid, amount, ixigoMoney);
			}
		} catch (Exception e) {
			logger.error("Exception occurred while determining hash key string for payment option : " + paymentOption, e);
			throw new Exception("Exception occurred while determining hash key string for payment option : " + paymentOption);
		}
	}

	private String getHKStringForCashPool(String ttid, String amount, PayNowRequest payNowRequest) {
		String userId = payNowRequest.getCashPoolUserId();
		String corpId = payNowRequest.getCashPoolCorpId();
		return new StringBuilder().append(ttid).append(amount).append(userId).append(corpId).toString();
		
	}

	private String getHKStringForCardPayment(Map<String, String> paymentParamsMap, String ttid, String amount, String cvvRequired, String isZeroAmountTxn, String ixigoMoney) throws Exception {
		String txnType = paymentParamsMap.get(PaymentUIUtil.TRANSACTION_TYPE);
		if(StringUtils.isNotBlank(txnType) && txnType.equalsIgnoreCase(PaymentUIUtil.TRANSACTION_TYPE_NON_MOTO)){
			return new StringBuilder().append(ttid).append(amount).toString();

		}
		String cardNumber = paymentParamsMap.get(PaymentConstants.CARD_NUMBER_PARAM);
		String cardExpiryYear = paymentParamsMap.get(PaymentConstants.CARD_EXPIRY_YEAR_PARAM);
		String cardExpiryMonth = paymentParamsMap.get(PaymentConstants.CARD_EXPIRY_MONTH_PARAM);
		if(YatraUtil.isNullOrEmpty(ixigoMoney))
			return new StringBuilder().append(cardNumber).append(cardExpiryMonth).append(cardExpiryYear).append(ttid).append(amount).append(cvvRequired).append(isZeroAmountTxn).toString();
		else return new StringBuilder().append(cardNumber).append(cardExpiryMonth).append(cardExpiryYear).append(ttid).append(amount).append(cvvRequired).append(isZeroAmountTxn).append(ixigoMoney).toString();
	}

	private String getHKStringForRewardPayment(Map<String, String> paymentParamsMap, String ttid, String amount, String ixigoMoney) throws Exception {

		String cardNumber = paymentParamsMap.get(PaymentConstants.CARD_NUMBER_PARAM);
		String cardExpiryYear = paymentParamsMap.get(PaymentConstants.CARD_EXPIRY_YEAR_PARAM);
		String cardExpiryMonth = paymentParamsMap.get(PaymentConstants.CARD_EXPIRY_MONTH_PARAM);
		String rewardAmount = paymentParamsMap.get(PaymentConstants.REWARD_AMOUNT_PARAM);
		rewardAmount = StringUtils.isEmpty(rewardAmount) ? PaymentConstants.ZERO_AMOUNT : rewardAmount;
		String cashAmount = String.valueOf(Double.valueOf(amount.trim()) - Double.valueOf(rewardAmount.trim()));
		if(YatraUtil.isNullOrEmpty(ixigoMoney))
			return new StringBuilder().append(cardNumber).append(cardExpiryMonth).append(cardExpiryYear).append(ttid).append(cashAmount).append(rewardAmount).toString();
		else return new StringBuilder().append(cardNumber).append(cardExpiryMonth).append(cardExpiryYear).append(ttid).append(cashAmount).append(rewardAmount).append(ixigoMoney).toString();
	}

	private String getHKStringForQBPayment(Map<String, String> paymentParamsMap, String ttid, String amount, String cvvRequired, String isZeroAmountTxn, String ixigoMoney) throws Exception {

		String cardId = paymentParamsMap.get(PaymentConstants.CARD_ID_PARAM);
		if(YatraUtil.isNullOrEmpty(ixigoMoney))
			return new StringBuilder().append(cardId).append(ttid).append(amount).append(cvvRequired).append(isZeroAmountTxn).toString();
		else return new StringBuilder().append(cardId).append(ttid).append(amount).append(cvvRequired).append(isZeroAmountTxn).append(ixigoMoney).toString();
	}
	
	private String getHKStringForCorpPayment(String ttid, String amount, String isZeroAmountTxn) throws Exception {
		return new StringBuilder().append(ttid).append(amount).append(isZeroAmountTxn).toString();
	}

	private String getHKString(String ttid, String amount, String ixigoMoney) throws Exception {
		if(YatraUtil.isNullOrEmpty(ixigoMoney))
			return new StringBuilder().append(ttid).append(amount).toString();
		else return new StringBuilder().append(ttid).append(amount).append(ixigoMoney).toString();
	}

	public String generateHashKey(String hashKeyInput) throws Exception {
		try {
			String hashKeyAlgo = yatraPropertyReader.getProperty("hash.key.algo");
			MessageDigest md5Digest = MessageDigest.getInstance(hashKeyAlgo);
			md5Digest.reset();
			md5Digest.update(hashKeyInput.getBytes());
			byte[] encrypted = md5Digest.digest();
			BASE64Encoder encoder = new BASE64Encoder();
			String base64 = encoder.encode(encrypted);
			return StringUtils.left(base64, base64.length() - 2);

		} catch (Exception e) {
			logger.error("Exception occurred while calculating the hash key : ", e);
			throw new Exception("Exception occurred while calculating the hash key : " + e);
		}
	}
}
