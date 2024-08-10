package com.yatra.payment.ui.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.ui.payswift.service.UpdateDBCacheService;

@Controller
public class UpdateDBCacheController {
	
	private static Logger logger = Logger.getLogger(UpdateDBCacheController.class);
	@Autowired UpdateDBCacheService updateDBCacheService;
	
	@RequestMapping(value ="updatePayOpMessageCache")
	@ResponseBody
	public String updatePayOpMessageCache(HttpServletRequest httpRequest){
		logger.debug("Entering the getBankDownMessage method");
		return updateDBCacheService.updatePayopMessageMappingCache();
	}

}
