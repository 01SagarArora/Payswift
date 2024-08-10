package com.yatra.payment.ui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.payswift.service.CommonPgDataGenerator;
import com.yatra.payment.ui.payswift.service.ProcessPaymentService;

@Controller
public class PayNowController {
	@Autowired
	CommonPgDataGenerator commonPgDataGenerator;
	@Autowired
	ProcessPaymentService processPaymentService;
	
	private static Logger logger = Logger.getLogger(PayNowController.class);

	@RequestMapping("payNow")
	@ResponseBody
	public PayNowResponse payNow(HttpServletRequest req, HttpServletResponse res, ModelAndView model) throws Exception {
		PayNowRequest payNowRequest = null;
		try {
			payNowRequest = new PayNowRequest(req);
			
			PayNowResponse payNowResponse = payNowResponse(payNowRequest);
			//Payment response generation complete, now forwarding to processPaymentService 
			payNowResponse = processPaymentService.processPayment(payNowRequest, payNowResponse);
			return payNowResponse;
		} catch (Exception ex) {
			logger.error("Pay Now api failed, Error: " + ex.getMessage(), ex);
			PayNowResponse payNowResponse = new PayNowResponse();
			payNowResponse.setSuccess(false);
			payNowResponse.setRedirect(false);
			payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
			payNowResponse.setFailureCode(FailureCode.GENERIC_EXCEPTION.setCustomCode(ex.getMessage()));
			return payNowResponse;
		}
	}

	/*
	 * functions of this API include . 1. Geenerate PG DATA MAP . 2. Do validate
	 * call with The LOB with parameters Mentioned in the document . 4. Wallet
	 * Amount Validation . 3. This Code will return rurl( payment Portal or LOB
	 * ) and parameter Map in JSON .
	 */

	@RequestMapping("appPayNow")
	public PayNowResponse getAppPGData(HttpServletRequest req, HttpServletResponse res, ModelAndView model)
			throws Exception {
		return commonPgDataGenerator.getPGData(new PayNowRequest(req));
	}

	public PayNowResponse payNowResponse(PayNowRequest payNowRequest) throws Exception {
		return commonPgDataGenerator.getPGData(payNowRequest);
	}
}
