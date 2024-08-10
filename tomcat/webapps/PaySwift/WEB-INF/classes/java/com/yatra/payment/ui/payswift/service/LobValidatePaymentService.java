package com.yatra.payment.ui.payswift.service;

import static com.yatra.payment.client.enums.ResponseStatus.FAILURE;
import static com.yatra.payment.client.enums.ResponseStatus.SUCCESS;
import static com.yatra.payment.ui.enums.FailureCode.VALIDATION_FROM_LOB_EMPTY_RESPONSE;
import static com.yatra.payment.ui.enums.FailureCode.VALIDATION_FROM_LOB_EXCEPTION;
import static com.yatra.payment.ui.enums.FailureCode.NO_FAILURE_CAPTURED;
import static com.yatra.payment.ui.util.PaymentUIUtil.VALIDATE_PAYMENT_WITH_LOB_RES;

import java.util.HashMap;
import java.util.Map;

import com.yatra.payment.ui.helper.CardsPaymentHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.client.PaymentClient;
import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.payswift.beans.CorporateAutoBookingRequest;
import com.yatra.payment.payswift.beans.LobValidateRequest;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.service.PaytmTokenUtilityService;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;


@Service
public class LobValidatePaymentService {

	@Autowired
	PayswiftStagesDAOImpl paymentDAO;
	@Autowired
	PaymentClient paymentClient;
	@Autowired
	private HttpService httpService;
	@Autowired
	LoggingProcessor loggingProcessor;
	@Autowired private UICacheManager<ProductServiceUrlBean> productServiceUrlCache;
	@Autowired
	private PropertyManager propertyManager;

	@Autowired private HttpUtil httpUtil;

	@Autowired
	PaytmTokenUtilityService paytmTokenUtilityService;
	@Autowired
	NoCostEmiService noCostEmiService;

	@Autowired
	CardsPaymentHelper cardsPaymentHelper;

	Logger logger = Logger.getLogger(LobValidatePaymentService.class);

	public LobValidationResponse serverSideValidateWithLob(PayNowRequest payNowRequest) {
		String response = "";
		LobValidationResponse lobResponseBean = null;
		String ttid = null;
		String superPnr = payNowRequest.getSuperPnr();
		String productCode = payNowRequest.getProductCode();
		ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(productCode);
		String payNowValidationUrl = payNowValidationUrl(payNowRequest, productServiceUrlBean);
		logger.info("Hitting LOB for validate payment details url " + payNowValidationUrl + " superPnr : " + superPnr);
		//Not using it currently
		Long requestTime = System.currentTimeMillis();
		ResponseStatus responseStatus = SUCCESS;
		String failureCodeMessage = NO_FAILURE_CAPTURED.toString();
		LobValidateRequest validateRequest = new LobValidateRequest(payNowRequest, paytmTokenUtilityService, noCostEmiService,cardsPaymentHelper);
		Map<String, String> parameterMap = validateRequest.getRequestMap();
		
		Map<String,String> loggingMap = PaymentUIUtil.getMapForLoggingPurpose(parameterMap);
		loggingProcessor.logIntoPayswiftStages(superPnr, PaymentUIUtil.VALIDATE_PAYMENT_WITH_LOB_REQ, payNowValidationUrl + "?" + PaymentUIUtil.createPostDataFromMapWithoutMaskedCard(loggingMap), productCode);

		try {
			response = validateRequestToProduct(payNowValidationUrl, parameterMap, productCode, productServiceUrlBean.getLOB());
			lobResponseBean = new LobValidationResponse(response);
			ttid = lobResponseBean.getTtid();
		} catch (PayswiftException pse) {
			responseStatus = FAILURE;
			failureCodeMessage = pse.getMessage();
			throw pse;
		}finally {
			//logDataInDb(superPnr, ttid, productCode, response, responseStatus, failureCodeMessage);
			long dbId = paymentDAO.insertIntoPaySwiftStagesReturnsId(superPnr, ttid, VALIDATE_PAYMENT_WITH_LOB_RES, response, productCode, responseStatus.toString(), failureCodeMessage);
			lobResponseBean.setDBId(String.valueOf(dbId));
		}
		return lobResponseBean;
	}

	public LobValidationResponse serverSideValidateWithLobForApp(PayNowRequest payNowRequest, String productCode) throws Exception {
		return null;
	}

	public LobValidationResponse serverSideValidateWithLob(CorporateAutoBookingRequest payNowRequest,String paymentOption)  {
		LobValidationResponse lobResponseBean = null;
		ResponseStatus responseStatus = SUCCESS;
		String failureCodeMessage = NO_FAILURE_CAPTURED.toString();
		String response = "";
		String ttid = null;
		String superPnr = payNowRequest.getSuperPnr();
		String productCode = payNowRequest.getProduct();
		ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(productCode);
		String payNowValidationUrl =  productServiceUrlBean.getLobValidateUrl();
		logger.info("Hitting LOB for validate payment details url " + payNowValidationUrl + " superPnr : " + superPnr);
		Map<String,String> parameterMap = new HashMap<String,String>();
		parameterMap.put(PaymentUIUtil.SUPER_PNR, payNowRequest.getSuperPnr());
		parameterMap.put(PaymentUIUtil.PAYMENT_OPTION_KEY_FOR_LOB, paymentOption);
		parameterMap.put(PaymentUIUtil.PRODUCT_CODE, payNowRequest.getProduct());
		parameterMap.put(PaymentUIUtil.SSO_TOKEN, payNowRequest.getSsoToken());
		if(StringUtils.isNotBlank(payNowRequest.getPromoCode())){
			parameterMap.put(PaymentUIUtil.PROMO_CODE, payNowRequest.getPromoCode());
		}
		
		Map<String,String> reqMap = removeParametersIfBlank(parameterMap);
		
		loggingProcessor.logIntoPayswiftStages(superPnr, PaymentUIUtil.VALIDATE_PAYMENT_WITH_LOB_REQ, payNowValidationUrl + "?" + PaymentUIUtil.createPostDataFromMap(reqMap), productCode);
		//Not using it currently
		Long requestTime = System.currentTimeMillis();
		try {
			response  = validateRequestToProduct(payNowValidationUrl, reqMap, productCode, productServiceUrlBean.getLOB());
			lobResponseBean = new LobValidationResponse(response);
			ttid = lobResponseBean.getTtid();
		} catch (PayswiftException pse) {
			responseStatus = FAILURE;
			failureCodeMessage = pse.getMessage();
			throw pse;
		}finally {
			//logDataInDb(superPnr, ttid, productCode, response, responseStatus, failureCodeMessage);
			long dbId = paymentDAO.insertIntoPaySwiftStagesReturnsId(superPnr, ttid, VALIDATE_PAYMENT_WITH_LOB_RES, response, productCode, responseStatus.toString(), failureCodeMessage);
			lobResponseBean.setDBId(String.valueOf(dbId));
		}
		return lobResponseBean;
	}

	private String validateRequestToProduct(String payNowValidationUrl, Map<String, String> parameterMap, String productCode, String lob) {
		String validateResponse = "";
		FailureCode failureCode = NO_FAILURE_CAPTURED;
		try {
			validateResponse = validateRequest(payNowValidationUrl, parameterMap, productCode, lob);
			logger.info("Response received from LOB are " + validateResponse);
		} catch (Exception ex) {
			failureCode = VALIDATION_FROM_LOB_EXCEPTION.setCustomCode(ex.getMessage());
			throw new PayswiftException(VALIDATION_FROM_LOB_EXCEPTION.toString(), failureCode.getCode(), ex);
		}

		if(YatraUtil.isNullOrEmpty(validateResponse)) {
			failureCode = VALIDATION_FROM_LOB_EMPTY_RESPONSE.setCustomCode("Empty validate payment response: " + validateResponse);
			throw new PayswiftException(VALIDATION_FROM_LOB_EMPTY_RESPONSE.toString(), failureCode.getCode());
		}
		return validateResponse;
	}

	private String validateRequest(String payNowValidationUrl, Map<String, String> parameterMap, String productCode, String lob) throws Exception {
		if (propertyManager.getPropertyAsBoolean("dummy.validate.booking.api")) {
			return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/ValidationResponse.txt"),"UTF-8");
		}
		String requestMethod = (productCode.equals(PaymentUIUtil.PRODUCT_B2B) || productCode.equals(PaymentUIUtil.PRODUCT_B2B_UPLOAD) || productCode.equals(PaymentUIUtil.PRODUCT_B2B_IMPORT_PNR) || productCode.equals(PaymentUIUtil.PRODUCT_RA_UPLOAD) || productCode.equals(PaymentUIUtil.PRODUCT_CORPORATE) || "FLIGHTS".equalsIgnoreCase(lob)) ? "GET" : "POST";
		/*Map<String, String> headers = new HashMap<>();
		headers.put("httpMethod", requestMethod);
		headers.put("contentType", PaymentUIUtil.CONTENT_TYPE_FORM_URLENCODED);
		headers.put("encoding", PaymentUIUtil.UTF_8_ENCODING);
		return httpUtil.invoke(payNowValidationUrl, parameterMap, headers, VALIDATE_PAYMENT);*/
		logger.info("Making " + requestMethod + "request for product " + productCode + " and lob " + lob);
		HttpEndPoint endPoint = new HttpEndPoint(payNowValidationUrl, requestMethod);
		return httpService.invoke(endPoint, parameterMap);
	}

	private String payNowValidationUrl(PayNowRequest payNowRequest, ProductServiceUrlBean productServiceUrlBean) {	
		//ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(payNowRequest.getBookingDetails().getProductCode());
		if(propertyManager.getPropertyAsBoolean("dynamic.lob.api.endpoint")){
			return StringUtils.isNotBlank(payNowRequest.getBookingDetails().getValidateAPIEndpoint())? payNowRequest.getBookingDetails().getValidateAPIEndpoint():productServiceUrlBean.getLobValidateUrl();
		}
		return productServiceUrlBean.getLobValidateUrl();
	}
	
	private Map<String, String> removeParametersIfBlank(
			Map<String, String> parameterMap) {
		
		Map<String,String> requestMap = new HashMap<String,String>();
		for (Map.Entry<String, String> entry : parameterMap.entrySet())
		{
			if (StringUtils.isNotBlank(entry.getValue())){
				requestMap.put(entry.getKey(),entry.getValue());
			}
		}
		return requestMap;
	}

}
