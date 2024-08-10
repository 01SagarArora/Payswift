package com.yatra.payment.client;

import java.util.Map;

import com.yatra.payment.client.service.CorporateCardTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yatra.payment.client.beans.CardHolderDetails;
import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.client.beans.WalletRequest;
import com.yatra.payment.client.service.HashKeyService;
import com.yatra.payment.client.service.PaymentHandshakeService;
import com.yatra.payment.client.service.WalletService;
import com.yatra.payment.client.service.impl.PaymentDivService;
import com.yatra.payment.payswift.beans.PayNowRequest;

@Component("paymentClient")
public class PaymentClient {
	
	@Autowired private PaymentDivService paymentDivServiceImpl;
	@Autowired private HashKeyService hashKeyService;
	@Autowired private PaymentHandshakeService paymentHandshakeService;
	@Autowired private WalletService walletService;

	@Autowired private CorporateCardTokenManager corporateCardTokenManager;


	
	
	public String generateHashKey(String paymentParams, String ttid, String amount, String cvvRequired, String isZeroAmountTxn, String ixigoMoney, PayNowRequest payNowRequest) throws Exception {
		return hashKeyService.getHashKey(paymentParams, ttid, amount, cvvRequired, isZeroAmountTxn, ixigoMoney,payNowRequest);
	}
	
	public String generateResponseHashKey(String paymentStatus, String amount, String merchantTxnRefId) throws Exception {
		return hashKeyService.getResponseHashKey(paymentStatus, amount, merchantTxnRefId);
	}

	public String getPaymentOptionJson(Map<String, String> paymentParamMap) throws Exception {
		return paymentDivServiceImpl.getPaymentOptionJson(paymentParamMap);
	}
	
	public String getPaymentOptionJson(String paymentParams) throws Exception {
		Map<String, String> paymentParamMap = getPaymentParamsMap(paymentParams);
		return getPaymentOptionJson(paymentParamMap);
	}
	
	public Map<String, String> getPromoCodeParamsMap(String promoCodePaymentParams) throws Exception {
		return paymentDivServiceImpl.getPromoCodeParamsMap(promoCodePaymentParams);
	}
	
	public Map<String, String> getPaymentParamsMap(String paymentParams) throws Exception {
		return paymentDivServiceImpl.getPaymentParamsMap(paymentParams);
	}
	
	public CardHolderDetails getCardHolderDetails(String paymentParams) throws Exception {
		return paymentDivServiceImpl.getCardHolderDetails(paymentParams);
	}
	
	public String handshake(String merchantCode, String productCode, String ttid, String merchantTxnRefId) throws Exception {
		return handshake(merchantCode, productCode, ttid, merchantTxnRefId, null);
	}
	
	public String handshake(String merchantCode, String productCode, String ttid, String merchantTxnRefId, String walletId) throws Exception {
		return paymentHandshakeService.handshake(merchantCode, productCode, ttid, merchantTxnRefId, walletId);
	}
	
	/*To support previous API for backward compatibility*/
	public WalletInfo getWalletInfo(String superPNR, String paymentParams) {
		return getWalletInfo(superPNR, paymentParams,false,"0");
	}
	
	public WalletInfo getWalletInfo(String superPNR, String paymentParams,Boolean validate ,String YLPMax) {
		return walletService.getWalletInfo(superPNR, paymentParams,validate,YLPMax);
	}
	
	public WalletInfo getWalletInfoByWalletId(String superPNR, String walletId, Boolean validate, String YLPMax) throws Exception{
		return walletService.getWalletInfoByWalletID(superPNR, walletId,validate,YLPMax);
	}
	
	public boolean completeWalletPayment(WalletRequest walletRequest) {
		return walletService.capture(walletRequest);
	}
	
	public boolean cancelWalletPayment(WalletRequest walletRequest) {
		return walletService.reverseAuth(walletRequest);
	}

	public Map<String,Object> getCorpCardTokenInfo(String cardid){
		return  corporateCardTokenManager.getCorpCardTokenInfo(cardid);
	}

}
