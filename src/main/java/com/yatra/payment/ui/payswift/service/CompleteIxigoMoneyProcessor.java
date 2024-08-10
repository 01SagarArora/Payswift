package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.helper.RequestResponseEnhancementHelper;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

@Service
public class CompleteIxigoMoneyProcessor implements PayNowProcessor {

	private static Logger logger = Logger.getLogger(CompleteIxigoMoneyProcessor.class);

        @Autowired
        private PayNowProcessor noCostEmiProcessor;
	@Autowired private PaymentUIHelper paymentUIHelper;
	@Autowired private RequestResponseEnhancementHelper rreHelper;
	@Autowired private IxigoMoneyService ixigoMoneyService;
	
	public static final String FULL_IXIGO_MONEY_PAYMENT_FAILURE_MSG = "Ixigo Money redemption failed";

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		try {
			payNowResponse = noCostEmiProcessor.process(payNowRequest, payNowResponse);

			if (payNowResponse.isSuccess() && checkFullIxigoMoneyPayment(payNowRequest, payNowResponse.getValidationResponse())) {
				logger.debug("inside Complete IxigoMoney Payment Scenario ");
				Map<String, String> portalMap = rreHelper.preparePortalMap(payNowRequest, payNowResponse);
				org.json.JSONObject response = ixigoMoneyService.redeemIxigoMoney(portalMap, payNowRequest.getMerchantCode(), payNowRequest.getBookingDetails().getProductCode());
				boolean isSuccess = false;
				if("true".equalsIgnoreCase(response.getString("suc")))
					isSuccess = true;
				if (isSuccess) {
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.COMPLETE_IXIGO_PAYMENT_SUCCESS);
					payNowRequest.setMtxnID(response.getString(PaymentUIUtil.MERCHANT_TXN_ID));
					return payNowResponse;
				} else {
					Map<String, String> resMap = new HashMap<String, String>();
					resMap.put("suc", "false");
					resMap.put("superPnr", payNowRequest.getSuperPnr());
					resMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
					paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, resMap, false);
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.COMPLETE_IXIGO_PAYMENT_FAILURE);
					payNowRequest.setMtxnID(response.getString(PaymentUIUtil.MERCHANT_TXN_ID));
				}
			}
			return payNowResponse;

		} catch (PayswiftException ex) {
			logger.error("Exception in CompleteIxigoMoneyProcessor, ValidateIxigoMoney failed for superPNR : " + payNowRequest.getSuperPnr() + ", Due to : " + ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("Generic Exception in CompleteIxigoMoneyProcessor, ValidateIxigoMoney failed for superPNR : " + payNowRequest.getSuperPnr() + ", Exception is: " + ex);
			throw new PayswiftException(FailureCode.IXIGO_MONEY_REDEEM_EXCEPTION.toString(), FailureCode.IXIGO_MONEY_REDEEM_EXCEPTION.setCustomCode(ex.getMessage()).getCode(), ex);
		}
	}

	private boolean checkFullIxigoMoneyPayment(PayNowRequest payNowRequest, LobValidationResponse lobValidationResponse) {
		logger.debug("Checking Complete IxigoMoney for payop : " + payNowRequest.getPaymentOption());
		WalletInfo walletInfo = payNowRequest.getWalletInfo();
		double walletAmount = 0d;
		if(payNowRequest.getWalletInfo() != null)
			walletAmount = Double.parseDouble(walletInfo.getAmountInRupees());
		logger.info("Checking complete ixigoMoney payment with walletAmount :" + walletAmount + " and ixigoMoney : " + payNowRequest.getBookingDetails().getIxigoMoney());

		if(!YatraUtil.isNullOrEmpty(payNowRequest.getBookingDetails().getIxigoMoney()) && Double.parseDouble(payNowRequest.getBookingDetails().getIxigoMoney()) > 0) {
			double totalWalletAndIxigoMoneyAmount = walletAmount + Double.parseDouble(payNowRequest.getBookingDetails().getIxigoMoney());
			if(PaymentUIUtil.PAYMENT_OPTION_IXIGO_MONEY.equals(payNowRequest.getPaymentOption()) &&
					PaymentUIUtil.validForFullRemeption(String.valueOf(totalWalletAndIxigoMoneyAmount), lobValidationResponse.getAmountToCollect())){
				return true;
			}
		}		
		return false;
	}

}
