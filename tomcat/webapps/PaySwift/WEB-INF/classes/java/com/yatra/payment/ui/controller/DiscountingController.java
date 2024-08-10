package com.yatra.payment.ui.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.beans.VisaCardValidationRequest;
import com.yatra.payment.payswift.beans.VisaCardValidationResponse;
import com.yatra.payment.ui.payswift.service.DiscountingService;
import com.yatra.payment.ui.payswift.service.ValidatorService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Controller
@RequestMapping("Discounting")
public class DiscountingController {
	private static Logger logger = Logger.getLogger(DiscountingController.class);

	@Autowired DiscountingService visaDiscountingService;
	@Autowired ValidatorService validatorService;
	
	@RequestMapping("validateVisaCard")
	@ResponseBody
	public VisaCardValidationResponse visaDiscountValidator(HttpServletRequest request){
		VisaCardValidationRequest visaCardValidationRequest = new VisaCardValidationRequest(request);
		String cardData = visaCardValidationRequest.getCardData();
		String ttid = visaCardValidationRequest.getTtid();
		String product = visaCardValidationRequest.getProduct();
		String payOp = visaCardValidationRequest.getPaymentOption();
		
		/*String errorMessage = validatorService.validateVisaParameters(cno,ttid,product);
		if(StringUtils.isNotBlank(errorMessage)){
			logger.error(errorMessage);
			return null;
		}*/
		logger.info("parameters Received are "+ PaymentUIUtil.maskCreditCardNumber(cardData)+" , "+ttid+" , "+ product);
		
		VisaCardValidationResponse visaCardValidationResponse;
		
			visaCardValidationResponse = visaDiscountingService.isCardValidForVisaDiscount(cardData,ttid,product,payOp);
		return visaCardValidationResponse;
	}

	
}
