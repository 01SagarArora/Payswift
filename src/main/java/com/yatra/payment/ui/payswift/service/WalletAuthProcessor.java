package com.yatra.payment.ui.payswift.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.service.WalletService;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.payswift.beans.WalletAuthResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.WalletHelper;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

import static com.yatra.payment.ui.util.PaymentUIUtil.PRODUCT_ECASH_GV;


@Service
public class WalletAuthProcessor implements PayNowProcessor {

	@Autowired
	private PayNowProcessor walletHandler;

	@Autowired
	private WalletHelper walletHelper;

	private static Logger logger = Logger.getLogger(WalletAuthProcessor.class);


	@Override
	public PayNowResponse process(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {
		String amountToRedeem;
		if(payNowRequest.getProductCode().equals(PRODUCT_ECASH_GV) && payNowRequest.getWalletAmountRedeemed() == null && payNowRequest.getWalletID() == null) {
			amountToRedeem = payNowRequest.getBookingDetails().getMaximumRedeemableECash();
			logger.info("AmountToRedeem for ecashGV: " + amountToRedeem);
		}else {
			amountToRedeem = payNowRequest.getAmountToRedeem();
		}
		
		if(isWalletAuthRequired(payNowRequest, amountToRedeem)) {
			payNowResponse = walletHelper.doWalletAuth(payNowRequest, payNowResponse, amountToRedeem);
			if (YatraUtil.isNullOrEmpty(payNowRequest.getWalletID())) {
				return payNowResponse;
			}
		}
		return walletHandler.process(payNowRequest, payNowResponse);
	}

	private boolean isWalletAuthRequired(PayNowRequest payNowRequest, String amountToRedeem) {
		if(amountToRedeem != null && Double.valueOf(amountToRedeem).intValue() > 0 && payNowRequest.getWalletAmountRedeemed() == null) {
			return true;
		}
		return false;
	}

	/*private PayNowResponse setFailPayNowResponse(PayNowResponse payNowResponse, String errorMessage, FailureCode failureCode) {
		payNowResponse.setSuccess(false);
		payNowResponse.setRedirect(false);
		payNowResponse.setMessage(errorMessage);
		payNowResponse.setFailureCode(failureCode);
		payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
		return payNowResponse;
	}*/

}
