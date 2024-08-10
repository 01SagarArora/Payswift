package com.yatra.payment.ui.payswift.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.exceptions.PayswiftException;
import  com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class ClientAmountVerifier implements PayNowProcessor {

	@Autowired
	private LobValidatePaymentService lobValidatePaymentService;

	private static Logger logger = Logger.getLogger(ClientAmountVerifier.class);

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {
		LobValidationResponse lobResponse = null;

			payNowRequest.validatePayNowRequest();
			lobResponse = lobValidatePaymentService.serverSideValidateWithLob(payNowRequest);

			payNowResponse.setValidationResponse(lobResponse);
			payNowResponse.setTtid(lobResponse.getTtid());
			
			if (!lobResponse.isSuccess()) {
				payNowResponse.setSuccess(false);
				payNowResponse.setFailureCode(lobResponse.getFailureCode());
				/*if(lobResponse.getFailureCode() == FailureCode.VALIDATION_FROM_LOB_EMPTY_RESPONSE
						|| lobResponse.getFailureCode() == FailureCode.VALIDATION_FROM_LOB_REQUIRED_PARAMS_MISSING
						|| lobResponse.getFailureCode() == FailureCode.VALIDATION_FROM_LOB_EXCEPTION){
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
				} else */
				if(lobResponse.shouldRedirect()){
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.VALIDATION_FAILURE_WITH_REDIRECT);
				}
				else
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.VALIDATION_FAILURE_WITHOUT_REDIRECT);
				return payNowResponse;
			} 
			if("true".equalsIgnoreCase(payNowRequest.isNoCostEmiPayment())) {
				double amountToCollect = Double.parseDouble(lobResponse.getAmountToCollect());
				double noCostEMIDiscount = Double.parseDouble(payNowRequest.getNoCostEmiDiscount());
				double noCostEMIActualAmount = Double.parseDouble(payNowRequest.getNoCostEmiActualAmount());
				double miscAmount = 0.0;
				//get miscellaneous amount by summing up walletAmount, IxigoMoney and VoucherAmount
				if(payNowRequest.getWalletInfo() != null)
					miscAmount = Double.parseDouble(payNowRequest.getWalletInfo().getAmountInRupees());
				if(payNowRequest.getVoucherRedeemedAmount() != null)
					miscAmount = miscAmount + Double.parseDouble(payNowRequest.getVoucherRedeemedAmount());
				if(payNowRequest.getBookingDetails().getIxigoMoney() != null)
					miscAmount = miscAmount + Double.parseDouble(payNowRequest.getBookingDetails().getIxigoMoney());
				logger.info("AmountToCollect - MiscAmount + NoCostEMIDiscount : " + amountToCollect + "-" + miscAmount + "+" + noCostEMIDiscount );
				logger.info("Actual Amount" + noCostEMIActualAmount);
				if((amountToCollect - miscAmount + noCostEMIDiscount < noCostEMIActualAmount - 200) 
						|| (amountToCollect - miscAmount + noCostEMIDiscount > noCostEMIActualAmount + 200)) {
					payNowResponse.setSuccess(false);
					payNowResponse.setFailureCode(FailureCode.NO_COST_EMI_AMOUNT_MISMATCH);
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
					return payNowResponse;
				}
			}
			payNowResponse.setSuccess(true);
			payNowResponse.setResponseDecision(ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL);

		return payNowResponse;
	}
}
