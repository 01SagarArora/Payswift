package com.yatra.payment.ui.helper;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.service.WalletService;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.payswift.beans.WalletAuthResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

@Service
public class WalletHelper {
	
	private static Logger logger = Logger.getLogger(WalletHelper.class);

	@Autowired WalletService walletService;
	
	public PayNowResponse doWalletAuth(PayNowRequest payNowRequest, PayNowResponse payNowResponse, String amountToRedeem) {
		int amount = Double.valueOf(amountToRedeem).intValue();
		logger.info("AmountToRedeem sent by LOB is : " + amount);

		if(amount > 0) {
			WalletAuthResponse walletAuthResponse = walletService.auth(payNowRequest);
			String status = walletAuthResponse.getStatus();

			if(status.equalsIgnoreCase("true")) {
				String walletId = walletAuthResponse.getWalletId().trim();
				if(!YatraUtil.isNullOrEmpty(walletId)){
					payNowRequest.setWalletID(walletId);
					payNowRequest.setWalletAmount(PaymentUIUtil.getAmountInRupeesStrTillSpecifiedDecimalPlaces(Double.valueOf(walletAuthResponse.getAmountInPaisa())/100, 0));
				}	
			}
			else if(walletAuthResponse.getStatus().equalsIgnoreCase("false")) {
				throw new PayswiftException(FailureCode.WALLET_AUTH_FAILED.getCode(), "Wallet Auth Failed");
			}

		}
		return payNowResponse;
	}
}
