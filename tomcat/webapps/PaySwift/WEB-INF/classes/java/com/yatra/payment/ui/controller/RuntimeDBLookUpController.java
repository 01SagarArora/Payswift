package com.yatra.payment.ui.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.beans.PaymentInfoBean;
import com.yatra.payment.ui.payswift.service.RuntimeDBLookUpService;

@Controller
public class RuntimeDBLookUpController {
	
	private static Logger logger = Logger.getLogger(RuntimeDBLookUpController.class);
	
	@Autowired public RuntimeDBLookUpService runtimeDBLookUpService;
	
	@RequestMapping(value ="getBankDownMessage")
	@ResponseBody
	public String getBankDownMessage(HttpServletRequest httpRequest){
		logger.debug("Entering the getBankDownMessage method");
		
			JSONObject bankMessageJson = runtimeDBLookUpService.getBankDownMessage();
			return bankMessageJson.toString();	
	}
	
	@RequestMapping(value ="getPayOpMessage")
	@ResponseBody
	public String getPayOpMessage(HttpServletRequest httpRequest){
		logger.debug("Entering the getPayOpMessage method");
		
			JSONObject payopMessageJson = runtimeDBLookUpService.getPayOpMessage();
			return payopMessageJson.toString();	
	}
	
	@RequestMapping(value ="getUIMessage")
	@ResponseBody
	public String getUIMessage(HttpServletRequest httpRequest){
		logger.debug("Entering the getUIMessage method");
		
			JSONObject payopMessageJson = runtimeDBLookUpService.getUIMessage();
			return payopMessageJson.toString();	
	}

}
