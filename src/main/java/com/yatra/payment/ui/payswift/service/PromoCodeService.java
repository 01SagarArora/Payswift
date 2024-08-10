package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.payswift.beans.PromoPartialValidateRequest;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

@Service
public class PromoCodeService {
	@Autowired private UICacheManager<ProductServiceUrlBean> productServiceUrlCache;
	@Autowired private LoggingProcessor loggingProcessor;
	@Autowired private HttpService httpService;
	Logger logger = Logger.getLogger(PromoCodeService.class);
	public String partialValidatePromoCode(PromoPartialValidateRequest promoPartialValidateRequest)  {
		String responseJSON = "";

		
		String productCode = promoPartialValidateRequest.getPromoCode();
		try {
			Map<String,String> parameterMap = populateParameterMap(promoPartialValidateRequest);
			logger.info("Hitting "+productCode+" for partial promocode validation of Super Pnr "+promoPartialValidateRequest.getSuperPnr());
			
			ProductServiceUrlBean productServiceUrlBean = productServiceUrlCache.get(productCode);
			String promoCodeValidateUrl = productServiceUrlBean.getPartialPromoValidateUrl();
			String postdata = promoCodeValidateUrl + "?" + PaymentUIUtil.createPostDataFromMap(promoPartialValidateRequest.getParameterMap());
			loggingProcessor.logIntoPayswiftStages(promoPartialValidateRequest.getSuperPnr(), PaymentUIUtil.PARTIAL_VALIDATE_PROMO_CODE_REQ, postdata, productCode);
			if (StringUtils.isBlank(promoCodeValidateUrl)) {
				responseJSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/PromoValidationResponse.txt"));
			} else {
				responseJSON = sendPostAccToProduct(promoCodeValidateUrl, parameterMap, productCode);
			}
			logger.info("Response received from LOB are " + responseJSON);
			loggingProcessor.logIntoPayswiftStages(promoPartialValidateRequest.getSuperPnr(), PaymentUIUtil.VALIDATE_PAYMENT_WITH_LOB_RES, responseJSON, productCode);

		} catch (Exception ex) {
			logger.error("An error occurred while sending request to LOB ");
		}
		
		

		return responseJSON;
	}

	
	private Map<String,String> populateParameterMap(PromoPartialValidateRequest promoPartialValidateRequest) {
		Map<String,String> parameterMap = new HashMap();
		parameterMap.put(PaymentUIUtil.SUPER_PNR,promoPartialValidateRequest.getSuperPnr());
		parameterMap.put(PaymentUIUtil.PROMO_CODE,promoPartialValidateRequest.getPromoCode());
		parameterMap.put("promoContext",PaymentUIUtil.PROMO_CODE_CONTEXT);
		parameterMap.put("paymentOption",promoPartialValidateRequest.getPaymentOption());
		parameterMap.put(PaymentUIUtil.PAYMENT_TYPE,promoPartialValidateRequest.getPaymentType());
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,promoPartialValidateRequest.getPaymentMode());
		parameterMap.put(PaymentUIUtil.CARD_BIN, promoPartialValidateRequest.getCardBin());
		return parameterMap;
	}


	public String sendPostAccToProduct(String serverValidationUrl, Map<String, String> parameterMap, String productCode) {
		String responseJSON = "";
		try {
			if (StringUtils.isBlank(serverValidationUrl)) {
				responseJSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/bookingDetail.txt"));
			} else {
				if (productCode.equals(PaymentUIUtil.PRODUCT_B2B) || productCode.equals(PaymentUIUtil.PRODUCT_CORPORATE)) {
					HttpEndPoint endPoint = new HttpEndPoint(serverValidationUrl, "GET");
					responseJSON = httpService.invoke(endPoint, parameterMap);
				} else {
					HttpEndPoint endPoint = new HttpEndPoint(serverValidationUrl, "POST");
					responseJSON = httpService.invoke(endPoint, parameterMap);
				}
			}
		} catch (Exception ex) {
			logger.error("An error ocurred while fetching or logging itinerary details ", ex);
			return "";

		}
		return responseJSON;
	}
	
}
