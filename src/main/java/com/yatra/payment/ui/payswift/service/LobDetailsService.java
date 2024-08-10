package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.platform.util.YatraUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.payswift.beans.LobDetailRequest;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

import static com.yatra.payment.client.enums.ResponseStatus.FAILURE;
import static com.yatra.payment.client.enums.ResponseStatus.SUCCESS;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_ITINERARY_EMPTY_RESPONSE;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_ITINERARY_EXCEPTION;
import static com.yatra.payment.ui.enums.FailureCode.NO_FAILURE_CAPTURED;
import static com.yatra.payment.ui.util.PaymentUIUtil.FETCH_ITINERARY;
import static com.yatra.payment.ui.util.PaymentUIUtil.FETCH_ITINERARY_RESPONSE;
import static java.lang.String.valueOf;

@Service
public class LobDetailsService {

	@Autowired private HttpUtil httpUtil;
	@Autowired
	private HttpService httpService;
	@Autowired 
	LoggingProcessor loggingProcessor;
	@Autowired	
	private UICacheManager<ProductServiceUrlBean> productServiceUrlCache;
	@Autowired
	private PropertyManager propertyManager;
	
	private static Logger logger = Logger.getLogger(LobDetailsService.class);

	public LobDetailResponse fetchItineraryDetailsFromLob(LobDetailRequest detailRequest) {

		ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(detailRequest.getProduct());
		String itineraryAndFareDetailsUrl = itineraryAndFareDetailsUrl(detailRequest, productServiceUrlBean);

		logger.info("Hitting LOB for fetching itinerary details url " + itineraryAndFareDetailsUrl + " superPnr : " + detailRequest.getSuperPnr());
		loggingProcessor.logIntoPayswiftStages(detailRequest.getSuperPnr(), PaymentUIUtil.FETCH_ITINERARY_REQUEST, itineraryAndFareDetailsUrl +"?"+detailRequest.toString(),detailRequest.getProduct());

		String response = "";
		LobDetailResponse lobDetailResponse;
		Long requestTime = System.currentTimeMillis();
		ResponseStatus responseStatus = SUCCESS;
		String failureCodeMessage = NO_FAILURE_CAPTURED.toString();
		try {
			response = itineraryRequestToProduct(itineraryAndFareDetailsUrl, detailRequest.getRequestMap(), detailRequest.getProduct(), productServiceUrlBean.getLOB());
			lobDetailResponse = new LobDetailResponse(response);
		} catch (PayswiftException pse) {
			responseStatus = FAILURE;
			failureCodeMessage = pse.getMessage();
			throw pse;
		}finally {
			logDataInDb(detailRequest.getSuperPnr(), detailRequest.getProduct(),requestTime, response, responseStatus, failureCodeMessage);
		}
		return lobDetailResponse;
	}


	private String itineraryAndFareDetailsUrl(LobDetailRequest detailRequest, ProductServiceUrlBean productServiceUrlBean) {
		//ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(detailRequest.getProduct());

		if(propertyManager.getPropertyAsBoolean("dynamic.lob.api.endpoint")){
			return StringUtils.isBlank(detailRequest.getEndpoint())? productServiceUrlBean.getItineraryDetailsUrl():detailRequest.getEndpoint();
		}
		return productServiceUrlBean.getItineraryDetailsUrl();
	}

	private String itineraryRequest(String itineraryAndFareDetailsUrl, Map<String, String> parameterMap, String productCode, String lob) throws Exception {
		if (propertyManager.getPropertyAsBoolean("dummy.booking.detail.api")) {
			return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/bookingDetail.txt"));
		}
		String requestMethod = (productCode.equals(PaymentUIUtil.PRODUCT_B2B)
				|| productCode.equals(PaymentUIUtil.PRODUCT_B2B_UPLOAD)
				|| productCode.equals(PaymentUIUtil.PRODUCT_B2B_IMPORT_PNR)
				|| productCode.equals(PaymentUIUtil.PRODUCT_RA_UPLOAD)
				|| productCode.equals(PaymentUIUtil.PRODUCT_CORPORATE)
				|| productCode.equals(PaymentUIUtil.PRODUCT_CORPORATE_BUS_ANDROID)
				|| productCode.equals(PaymentUIUtil.PRODUCT_CORPORATE_BUS_IOS)
				|| productCode.equals(PaymentUIUtil.PRODUCT_CODE_CORPORATE_BUS_DESKTOP)
				|| productCode.equals(PaymentUIUtil.PRIME_DESKTOP)
				|| "FLIGHTS".equalsIgnoreCase(lob)) ? "GET" : "POST";
		
		if(productCode.equalsIgnoreCase("trainb2c") || productCode.equalsIgnoreCase("trainb2b"))
			parameterMap.put(PaymentUIUtil.PRODUCT_CODE, productCode);
		
		logger.info("Making " + requestMethod + "request for product " + productCode + " and lob " + lob);
		HttpEndPoint endPoint = new HttpEndPoint(itineraryAndFareDetailsUrl, requestMethod);
		return httpService.invoke(endPoint, parameterMap);
	}

	private void logDataInDb(String superpnr, String productCode, Long requestTime, String itineraryResponse, ResponseStatus responseStatus, String failureCodeMessage) {
		loggingProcessor.logIntoPayswiftStagesSerialZipped(superpnr, FETCH_ITINERARY_RESPONSE, itineraryResponse, productCode, responseStatus.toString(), failureCodeMessage);
		loggingProcessor.logIntoPayswiftLatency(superpnr, null, FETCH_ITINERARY, productCode, valueOf((System.currentTimeMillis() - requestTime)));
	}

	private String itineraryRequestToProduct(String itineraryAndFareDetailsUrl, Map<String, String> parameterMap, String productCode, String lob) {
		String itineraryResponse = "";
		FailureCode failureCode = NO_FAILURE_CAPTURED;
		try {
			itineraryResponse = itineraryRequest(itineraryAndFareDetailsUrl, parameterMap, productCode, lob);
			logger.info("Response received from LOB are " + itineraryResponse);
		} catch (Exception ex) {
			failureCode = FETCH_ITINERARY_EXCEPTION.setCustomCode(ex.getMessage());
			throw new PayswiftException(FETCH_ITINERARY_EXCEPTION.toString(), failureCode.getCode(), ex);
		}

		if(YatraUtil.isNullOrEmpty(itineraryResponse)) {
			failureCode = FETCH_ITINERARY_EMPTY_RESPONSE.setCustomCode("Empty itinerary response: " + itineraryResponse);
			throw new PayswiftException(FETCH_ITINERARY_EMPTY_RESPONSE.toString(), failureCode.getCode());
		}
		return itineraryResponse;
	}
}
