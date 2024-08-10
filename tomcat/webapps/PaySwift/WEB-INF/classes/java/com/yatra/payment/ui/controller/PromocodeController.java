package com.yatra.payment.ui.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.beans.PromoPartialValidateRequest;
import com.yatra.payment.ui.payswift.service.PromoCodeService;
import com.yatra.payment.ui.payswift.service.ValidatorService;

@Controller
@RequestMapping("promo")
public class PromocodeController {
	
	@Autowired ValidatorService validatorService;
	@Autowired PromoCodeService promoCodeService;
	
	Logger logger = Logger.getLogger(ValidatorService.class);
	@RequestMapping("partialValidate")
	@ResponseBody
	public String partialValidate(HttpServletRequest request) throws IOException{
		PromoPartialValidateRequest partialPromoRequest = new PromoPartialValidateRequest(request);
		String error = validatorService.validatePromoCodeParameters(partialPromoRequest);
		if(StringUtils.isNotBlank(error)){
			logger.error("Validation failed "+error);
			//return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/PromoValidationResponse.txt"));
			return "";
		}
		logger.info("Request Received for partial Promo Code Validation ");
		String response = promoCodeService.partialValidatePromoCode(partialPromoRequest);
		
		return response;
	}

}
