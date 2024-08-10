package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.PaymentClient;
import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.client.beans.WalletRequest;
import com.yatra.payment.client.service.WalletService;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.helper.WalletHelper;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

@Service
public class WalletHandler implements PayNowProcessor {
	
	@Autowired
	private PayNowProcessor completeIxigoMoneyProcessor;
	
	@Autowired
	private PayNowProcessor walletAuthProcessor;

	@Autowired
	private PaymentClient paymentClient;
	
	@Autowired
	private PaymentUIHelper paymentUIHelper;
	
	@Autowired 
	WalletService walletService;
	
	@Autowired
	private WalletHelper walletHelper;
	
	private static Logger logger = Logger.getLogger(WalletHandler.class);

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {
		
		LobDetailResponse bookingDetails = payNowRequest.getBookingDetails();
		String walletId = payNowRequest.getWalletID();
		/*if(YatraUtil.isNullOrEmpty(walletId))
			walletId = payNowRequest.getAuthWalletID();*/
		WalletInfo walletInfo = null;
		boolean walletInfoStatus;
		if (StringUtils.isNotBlank(walletId)) {
			try {
				walletInfo = paymentClient.getWalletInfo(payNowRequest.getSuperPnr(),payNowRequest.getPaymentOptionParameters(), true,bookingDetails.getMaximumRedeemableECash());
				walletInfoStatus = walletInfo.getStatus().equalsIgnoreCase("true");
				if (!walletInfoStatus) {
					logger.error("Wallet handshake failed, trying for to auth again with amonut : " + payNowRequest.getWalletAmountRedeemed());
					payNowRequest.setAmountToRedeem(payNowRequest.getWalletAmountRedeemed());
					payNowResponse = walletHelper.doWalletAuth(payNowRequest, payNowResponse, payNowRequest.getWalletAmountRedeemed());
					walletInfo = paymentClient.getWalletInfoByWalletId(payNowRequest.getSuperPnr(), payNowRequest.getWalletID(), true, bookingDetails.getMaximumRedeemableECash());
					walletInfoStatus = walletInfo.getStatus().equalsIgnoreCase("true");
				}

				if (!PaymentUIUtil.areAmountEqual(walletInfo.getAmountInRupees(), payNowRequest.getWalletAmountRedeemed()) || !walletInfoStatus) {
					doWalletReverseAuth(payNowRequest, walletInfo);
					logger.error("Ewallet handshake fail for walletId :" + walletId + " for superPnr : " + payNowRequest.getSuperPnr());
					throw new PayswiftException(FailureCode.WALLET_HANDSHAKE_FAILED.getCode(), "Wallet Handshake Failed");
				}
			payNowRequest.setWalletInfo(walletInfo);
			}
			catch(Exception e) {
				throw new PayswiftException(FailureCode.WALLET_AUTH_EXCEPTION.getCode(), "Exception in Wallet Auth Call", e);
			}
		}
		payNowResponse = completeIxigoMoneyProcessor.process(payNowRequest, payNowResponse);
	

		if (payNowResponse.isSuccess() && checkFullWalletPayment(payNowRequest, payNowResponse.getValidationResponse())) {
			logger.debug("inside Complete Wallet Payment Scenario ");
			boolean status = doCompleteWalletCapture(payNowRequest,  payNowResponse.getValidationResponse());
			if (status) {
				payNowResponse.setResponseDecision(ResponseDecisionScenarios.COMPLETE_WALLET_SUCCESS);
				return payNowResponse;
			} else {
				//TODO do a re auth and capture instead
				doWalletReverseAuth(payNowRequest,walletInfo);
				Map<String, String> resMap = new HashMap<String, String>();
				resMap.put("suc", "false");
				resMap.put("superPnr", payNowRequest.getSuperPnr());
				resMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
				paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, resMap, false);
				throw new PayswiftException(FailureCode.WALLET_CAPTURE_FAILED.getCode(), "Wallet Capture Failed");
			}
		}
		return payNowResponse;
	}

	private boolean checkFullWalletPayment(PayNowRequest payNowRequest, LobValidationResponse lobValidationResponse) {
		logger.debug("Inside Complete wallet "+payNowRequest.getPaymentOption());
		WalletInfo walletInfo = payNowRequest.getWalletInfo();
		if(walletInfo!=null && payNowRequest.getPaymentOption().equalsIgnoreCase("ew")&&
			PaymentUIUtil.validForFullRemeption(walletInfo.getAmountInRupees(), lobValidationResponse.getAmountToCollect())){
			return true;
		}
		return false;
	}

	private void doWalletReverseAuth(PayNowRequest payNowRequest, WalletInfo walletInfo) {
		LobDetailResponse bookingDetails = payNowRequest.getBookingDetails();
		logger.info("Doing reverse auth. Wallet Hand Shake Failed , received this from wallet service."	+ walletInfo.getMessage());
		WalletRequest walletRequest = new WalletRequest();
		walletRequest.setAmountInRupees(walletInfo.getAmountInRupees());
		walletRequest.setMerchantCode(bookingDetails.getMerchantCode());
		walletRequest.setProductCode(bookingDetails.getProductCode());
		walletRequest.setSuperPNR(payNowRequest.getSuperPnr());
		walletRequest.setUserEmail(bookingDetails.getUserEmail());
		walletRequest.setWalletId(walletInfo.getWalletId());
		paymentClient.cancelWalletPayment(walletRequest);
	}
	
	
	private boolean doCompleteWalletCapture(PayNowRequest payNowRequest, LobValidationResponse vaidationResponse) {
		LobDetailResponse bookingDetails = payNowRequest.getBookingDetails();
		WalletInfo walletInfo = payNowRequest.getWalletInfo();
		logger.info("Doing complete wallet Payment for ttid "	+ vaidationResponse.getTtid());
		WalletRequest walletRequest = new WalletRequest();
		walletRequest.setAmountInRupees(walletInfo.getAmountInRupees());
		walletRequest.setMerchantCode(bookingDetails.getMerchantCode());
		walletRequest.setProductCode(bookingDetails.getProductCode());
		walletRequest.setSuperPNR(payNowRequest.getSuperPnr());
		walletRequest.setUserEmail(bookingDetails.getUserEmail());
		walletRequest.setWalletId(walletInfo.getWalletId());
		walletRequest.setTtid(vaidationResponse.getTtid());
		return paymentClient.completeWalletPayment(walletRequest);
	}

}
