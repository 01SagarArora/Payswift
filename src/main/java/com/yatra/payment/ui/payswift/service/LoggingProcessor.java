package com.yatra.payment.ui.payswift.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.FutureTasks.DbLogger;
import com.yatra.payment.payswift.FutureTasks.PayStatsDBLogger;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.dao.impl.PayswiftLatencyDAOImpl;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class LoggingProcessor implements PayNowProcessor,PaymentProcessor {

	@Autowired
	private PayswiftStagesDAOImpl paymentDAO;

	@Autowired
	private PayNowProcessor requestResponseEnhancer;
	
	@Autowired
	private PaymentProcessor paymentProcessorImpl;

	@Autowired @Qualifier("dbLoggingthreadPoolTaskExecutor")
	private ThreadPoolTaskExecutor dbLoggingThreadPoolTaskExecutor;
	
	@Autowired
	private PayswiftLatencyDAOImpl payswiftLatencyDAO;
	
	private static Logger logger = Logger.getLogger(LoggingProcessor.class);
	

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {

		try{
			// log request
			logIntoPayswiftStages(payNowRequest.getSuperPnr(),PaymentUIUtil.PAY_NOW_REQ, payNowRequest.toString(),payNowRequest.getProductCode());
			logger.debug("Request logging in DB complete , now forwarding the request to request enhancer");
			payNowResponse = requestResponseEnhancer.process(payNowRequest, payNowResponse);
		}
		catch(PayswiftException pe){
			logger.error("Pay Now api failed for superpnr: " + payNowRequest.getSuperPnr() + ", product: " + payNowRequest.getProductCode() + ", Error: " + pe.getMessage(), pe);
			payNowResponse.setSuccess(false);
			payNowResponse.setRedirect(false);
			payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
			payNowResponse.setFailureCode(FailureCode.valueOf(pe.getErrorCode()));
		}
		catch(Exception e){
			logger.error("Error inside paynow response "+ payNowRequest.getSuperPnr(),e);
			payNowResponse.setSuccess(false);
			payNowResponse.setRedirect(false);
			payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
			payNowResponse.setFailureCode(FailureCode.GENERIC_EXCEPTION.setCustomCode(e.getMessage()));
		}
		// log response 
		logIntoPayswiftStages(payNowRequest.getSuperPnr(), payNowResponse.getTtid(), 
				PaymentUIUtil.PAY_NOW_RES, payNowResponse.toString(), payNowRequest.getProductCode(),
				payNowResponse.getResponseStatus().toString(), payNowResponse.getFailureCode().getCode());

		return payNowResponse;
	}

	@Override
	public PayNowResponse processPayment(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		
		// Log request in db
		logIntoPayswiftStages(payNowRequest.getSuperPnr(),PaymentUIUtil.PAY_REQ, payNowRequest.toString(),payNowRequest.getProductCode());
		logger.debug("Request logging in DB complete , now forwarding the request to payment processor Impl");
		try{
			// Hit Payment Portal to process payment
			payNowResponse = paymentProcessorImpl.processPayment(payNowRequest, payNowResponse);
		}catch(Exception e){
			logger.error("Error inside paynow response "+ payNowRequest.getSuperPnr(),e);
			payNowResponse.setSuccess(false);
			payNowResponse.setRedirect(false);
			payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
		}
		// Log response 
		logIntoPayswiftStages(payNowRequest.getSuperPnr(), PaymentUIUtil.PAY_RES, payNowResponse.toString(), payNowRequest.getProductCode());

		return payNowResponse;
	}

	public void logIntoPayswiftStages(String superPnr, String stage, String params, String product) {
		dbLoggingThreadPoolTaskExecutor.submit(new DbLogger(superPnr, null, stage, params, product, null, null, paymentDAO));
	}

	public void logIntoPayswiftStages(String superPnr, String ttid, String stage, String params, String product, String responseStatus, String failureCode) {
		dbLoggingThreadPoolTaskExecutor.submit(new DbLogger(superPnr, ttid, stage, params, product, responseStatus, failureCode, paymentDAO));
	}
	
	public void logIntoPayswiftStagesSerialZipped(String superPnr, String stage, String params, String product, String responseStatus, String failureCode) {
		try {
			paymentDAO.insertIntoPayswiftStagesZipped(superPnr, null, stage, params, product, responseStatus, failureCode);
		} catch (Exception ex) {
			logger.error("For superpnr: " + superPnr + ", product: " + product + ", Error: " + ex.getMessage(), ex);
		}
	}

	public void logIntoPayswiftLatency(String superPnr, String ttid, String stage, String product, String responseTime) {
		dbLoggingThreadPoolTaskExecutor.submit(new PayStatsDBLogger(superPnr, ttid, stage, product, responseTime, payswiftLatencyDAO));
	}
}
