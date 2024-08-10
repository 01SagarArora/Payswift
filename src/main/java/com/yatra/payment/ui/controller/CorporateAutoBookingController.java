package com.yatra.payment.ui.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.beans.CorporateAutoBookingRequest;
import com.yatra.payment.payswift.beans.CorporateAutoBookingResponse;
import com.yatra.payment.ui.payswift.service.CorporateAutoBookingService;
import com.yatra.payment.ui.payswift.service.LoggingProcessor;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Controller
public class CorporateAutoBookingController {
	
	private static final Logger logger = Logger.getLogger(CorporateAutoBookingController.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired private CorporateAutoBookingService corporateAutoBookingService;
	@Autowired private LoggingProcessor loggingProcessor;
	
	@RequestMapping(value ="corporate-auto-booking", method=RequestMethod.GET)
	@ResponseBody CorporateAutoBookingResponse autoBooking(HttpServletRequest request){
		
		CorporateAutoBookingRequest corporateAutoBookingRequest = new CorporateAutoBookingRequest(request);
		logger.info("Entering autoBooking() method ,paramter received from lob = "+request.getQueryString());
		loggingProcessor.logIntoPayswiftStages(corporateAutoBookingRequest.getSuperPnr(),
				PaymentUIUtil.PAY_NOW_REQ, request.getQueryString(),corporateAutoBookingRequest.getProduct() );
		
		CorporateAutoBookingResponse response = corporateAutoBookingService.autoBooking(corporateAutoBookingRequest);
		
		logger.info("Returning from autoBooking method , response send to lob =  "+response);
		try {
			loggingProcessor.logIntoPayswiftStages(corporateAutoBookingRequest.getSuperPnr(),
					PaymentUIUtil.PAY_NOW_RES, objectMapper.writeValueAsString(response),corporateAutoBookingRequest.getProduct() );
		} catch (IOException e) {
			logger.error("Exception while converting CorporateAutoBookingResponse object to json string",e);
		}
		return response;
		
	}
	
}
