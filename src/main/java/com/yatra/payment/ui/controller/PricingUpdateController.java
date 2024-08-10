package com.yatra.payment.ui.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.beans.GSTUpdateRequest;
import com.yatra.payment.payswift.beans.PromoPartialValidateRequest;
import com.yatra.payment.ui.payswift.service.PricingUpdateService;
import com.yatra.payment.ui.payswift.service.ValidatorService;

@Controller
@RequestMapping("pricingUpdate")
public class PricingUpdateController {
	@Autowired PricingUpdateService pricingUpdateService;

	Logger logger = Logger.getLogger(ValidatorService.class);
	@RequestMapping("GST")
	@ResponseBody
	public String updateGST(HttpServletRequest request) {
		GSTUpdateRequest gstUpdateRequest = new GSTUpdateRequest(request);
		logger.info("Request received for getting Updated FareBreakUp for superPnr" + gstUpdateRequest.getSuperPnr() + " and product : " + gstUpdateRequest.getProduct());
		return pricingUpdateService.getUpdatedFareBreakUp(gstUpdateRequest);	
	}

}
