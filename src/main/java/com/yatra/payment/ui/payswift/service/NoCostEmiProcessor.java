package com.yatra.payment.ui.payswift.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.RequestResponseEnhancementHelper;
import com.yatra.payment.ui.util.PaymentUIUtil;
import org.json.JSONObject;

@Service
public class NoCostEmiProcessor implements PayNowProcessor {

	private static Logger logger = Logger.getLogger(NoCostEmiProcessor.class);

	@Autowired
	private PayNowProcessor giftVoucherProcessor;
	@Autowired private NoCostEmiService noCostEmiService;
	@Autowired private RequestResponseEnhancementHelper rreHelper;

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		try {
                    boolean error = false;
                    if ("true".equalsIgnoreCase(payNowRequest.isNoCostEmiPayment())) {
                        error = noCostEmiService.fetchNoCostEmiParams(payNowRequest);
                        logger.info("Error value while processing no costemi payment : " + error + " superPnr: " + payNowRequest.getSuperPnr());
                        if(error) {
                        	payNowRequest.setIsNoCostEmiPayment("false");
                        }
                    }
                    else if (payNowRequest.getPaymentOption().equalsIgnoreCase("emi")){
                        error = noCostEmiService.fetchNoCostEmiParams(payNowRequest);
                        logger.info("Error value while processing no cost emi payment : " + error + " superPnr: " + payNowRequest.getSuperPnr());
                        if(!error) {
                            payNowRequest.setIsNoCostEmiPayment("true");
                        }
                    }

                    
                    payNowResponse = giftVoucherProcessor.process(payNowRequest, payNowResponse);

                    if ("true".equalsIgnoreCase(payNowRequest.isNoCostEmiPayment())&& !error && payNowResponse.isSuccess() && ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())) {
                        JSONObject params = new JSONObject();
                        params.put(PaymentUIUtil.IS_NO_COST_EMI_PAYMENT, payNowRequest.getIsNoCostEmiPayment());
                        params.put(PaymentUIUtil.NO_COST_EMI_CODE, payNowRequest.getNoCostEmiCode());
                        params.put(PaymentUIUtil.NO_COST_EMI_DISCOUNT, payNowRequest.getNoCostEmiDiscount());
                        params.put(PaymentUIUtil.EMI_BANK, payNowRequest.getEMIBankCode());
                        params.put(PaymentUIUtil.EMI_TENURE, payNowRequest.getEMITenure());
                        String ttid = payNowResponse.getTtid();
                        Number id = noCostEmiService.insertIntoNoCostEmiAudit(ttid, params.toString(), payNowRequest.getNoCostEmiDiscount());
                        String idInString = String.valueOf(id);
                        payNowResponse.putInPgData("idNoCostEmi", idInString);
                        payNowResponse.putInPgData(PaymentUIUtil.IS_NO_COST_EMI_PAYMENT, payNowRequest.getIsNoCostEmiPayment());
                    }
                 
                    return payNowResponse;

		} catch (PayswiftException ex) {
			logger.error("Exception in NoCostEmi Payment, failed for superPNR : " + payNowRequest.getSuperPnr() + ", Due to : " + ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("Generic Exception in NoCostEmiProcessor, No failed for superPNR : " + payNowRequest.getSuperPnr() + ", Exception is: " + ex);
			throw new PayswiftException(FailureCode.NO_COST_EMI_EXCEPTION.toString(), FailureCode.NO_COST_EMI_EXCEPTION.setCustomCode(ex.getMessage()).getCode(), ex);
		}
        }
}
