package com.yatra.payment.ui.payswift.service;

import java.util.List;

import com.yatra.payment.ui.exceptions.PayswiftException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PaymentRequest;
import com.yatra.payment.payswift.beans.PromoPartialValidateRequest;
import com.yatra.platform.commons.property.PropertyManager;

import static com.yatra.payment.ui.enums.FailureCode.PAGE_LOAD_REQUEST_PARAMS_VALIDATION_FAILED;
import static com.yatra.payment.ui.util.PaymentUIUtil.DEFAULT_YATRA_MERCHANT;

@Service
public class ValidatorService {

	@Autowired	private PropertyManager  propertyManager;
	Logger logger = Logger.getLogger(ValidatorService.class);

	public void validatePageLoadRequest(PaymentRequest request, List<String> b2bArrayList){
		String error = null;
		error = validateSuperPnr(request);
		error += validateProductCode(request);
		error += validateSsoTokenForB2bProductsAndYatraMerchant(request, b2bArrayList);

		if (StringUtils.isNotBlank(error)) {
			throw new PayswiftException(PAGE_LOAD_REQUEST_PARAMS_VALIDATION_FAILED.getCode(), error);
		}
	}

	private String validateSsoTokenForB2bProductsAndYatraMerchant(PaymentRequest request, List<String> b2bArrayList) {
		if(StringUtils.isNotBlank(request.getProductCode())
				&& b2bArrayList.contains(request.getProductCode()) 
				&& DEFAULT_YATRA_MERCHANT.equalsIgnoreCase(request.getMerchantCode())
				&& !request.getProductCode().equalsIgnoreCase("corpSAPG")){
			String ssoToken =  request.getSsoToken();
			if(StringUtils.isBlank(ssoToken)){
				logger.debug("ssoToken not found for "+ request.getTtid());
				return propertyManager.getProperty("error.sso.token.not.present");
			}
		}
		return "";
	}

	private String validateProductCode(PaymentRequest request) {
		if (StringUtils.isBlank(request.getProductCode())) {
			logger.debug("product code not found ");
			return propertyManager.getProperty("error.productCode.not.present");
		}
		else
		{
			return "";
		}
	}

	private String validateSuperPnr(PaymentRequest request) {
		String superPnr = request.getSuperPnr();
		if(StringUtils.isBlank(superPnr)){
			logger.debug("Super PNR validation Failed");
			return propertyManager.getProperty("error.superPnr.absent");
		}
		return "";
	}

	public String validateVisaParameters(String cno, String superPnr, String product) {
		String error = null;
		
		if(StringUtils.isBlank(product)){
			error+="Product is missing";
		}
/*		if(StringUtils.isBlank(cno)){
			error+="Card Number is missing";
		}
*/		if(StringUtils.isBlank(superPnr)){
			error+="SuperPnr is missing";
		}
		
		return error;
	}

	public String validatePromoCodeParameters(PromoPartialValidateRequest req) {
		String error = null;
		
		if(StringUtils.isBlank(req.getProduct())){
			error+="Product is missing ,";
		}
		if(StringUtils.isBlank(req.getSuperPnr())){
			error+="SuperPNR is missing ,";
		}
		if(StringUtils.isBlank(req.getPromoCode())){
			error+="PromoCode is missing ,";
		}
		
		
		return error;
	}
	
}
