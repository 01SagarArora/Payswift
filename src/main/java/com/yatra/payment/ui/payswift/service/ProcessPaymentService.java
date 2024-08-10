package com.yatra.payment.ui.payswift.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.helper.RequestResponseEnhancementHelper;

@Service
public class ProcessPaymentService {

	public static String STATUS_TRUE = "true";
	
	@Autowired private PaymentProcessor loggingProcessor;
	@Autowired private AmazonPayMobileSDKService amazonPayMobileSDKService;
    @Autowired private GoogleTezService googleTezService;
    @Autowired private PhonePeService phonePeService;
    @Autowired private PaytmPaymentService paytmPaymentService;
    @Autowired private CredPaymentService credPaymentService;
	public PayNowResponse processPayment(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		
		// Check if it is required to hit Payment Portal to initiate payment
		if (isPaymentRequired(payNowRequest) && (ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision()))) {
			return loggingProcessor.processPayment(payNowRequest, payNowResponse);
		}
		
		if(payNowRequest.isAmazonPaySDKFlow() && ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())){
			return amazonPayMobileSDKService.processPayment(payNowRequest, payNowResponse);
		}
		
        if ((payNowRequest.isGoogleTezFlow() || payNowRequest.isGoogleTezAndroidFlow()) && ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())){
			return googleTezService.processPayment(payNowRequest, payNowResponse);
		}
        if(payNowRequest.isPhonePeAndroidFlow() &&ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())) {
			return phonePeService.processPayment(payNowRequest, payNowResponse, false);
		}
        if(payNowRequest.isPhonePeSwitchFlow() &&ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())) {
			return phonePeService.processPayment(payNowRequest, payNowResponse, true);
		}
        if(payNowRequest.isCredAppFlow() &&ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())) {
			return credPaymentService.processPayment(payNowRequest, payNowResponse);
		}
        if("mw".equalsIgnoreCase(payNowRequest.getPaymentOption()) && "PTM".equalsIgnoreCase(payNowRequest.getBankCode()) 
        		&& !payNowRequest.getProductCode().contains("monument")
        		&& ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())) {
			return paytmPaymentService.processPayment(payNowRequest, payNowResponse, true);
		}
        if("payltr".equalsIgnoreCase(payNowRequest.getPaymentOption()) && "PTMP".equalsIgnoreCase(payNowRequest.getBankCode()) 
        		&& !payNowRequest.getProductCode().contains("monument")
        		&& ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL.equals(payNowResponse.getResponseDecision())) {
			return paytmPaymentService.processPayment(payNowRequest, payNowResponse, true);
		}
                
		return payNowResponse;
	}

	public boolean isPaymentRequired(PayNowRequest payNowRequest) {

		String isS2SRequired = payNowRequest.getParameter("initPayment");
		if (STATUS_TRUE.equalsIgnoreCase(isS2SRequired)) {
			return true;
		}
		return false;
	}
}
