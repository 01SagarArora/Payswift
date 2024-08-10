package com.yatra.payment.ui.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.ui.builder.PaymentDivBuilder;

@Controller
public class RefreshPropertyController {
	
	@Autowired PaymentDivBuilder paymentDivBuilder;
	Logger logger = Logger.getLogger(RefreshPropertyController.class);
	@RequestMapping(value="refreshPaySwiftCache")
	@ResponseBody
	public String refreshPaySwiftCache(){
		logger.debug("Refreshing All types of cache in PaySwift .");
		paymentDivBuilder.build();
		logger.debug("CACHE REFRESHED SUCCESSFULLY");
		return "CACHE REFRESHED SUCCESSFULLY";
	}

}
