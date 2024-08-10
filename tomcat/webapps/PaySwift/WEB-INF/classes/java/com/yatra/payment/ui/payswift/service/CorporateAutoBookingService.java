package com.yatra.payment.ui.payswift.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.crp.cards.client.B2BExpressCardsServiceClient;
import com.yatra.express.crp.cards.v3.beans.CorporateCardsDisplayResponse;
import com.yatra.express.crp.cards.v3.beans.ResponseStatus;
import com.yatra.payment.payswift.beans.CorporateAutoBookingRequest;
import com.yatra.payment.payswift.beans.CorporateAutoBookingResponse;
import com.yatra.payment.payswift.beans.LobDetailRequest;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.ValidationResponse;
import com.yatra.payment.ui.util.ErrorMessageUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;


@Service
public class CorporateAutoBookingService {

	public static final String INSUFFICIENT_CREDIT_POOL_BALANCE_CODE = "INSUFFICIENT_CREDIT_POOL_BALANCE";
	public static final String INSUFFICIENT_CREDIT_POOL_BALANCE_MSG = "Unfortunately, your company's credit pool does not have sufficient funds to cover this booking.";
	private static final String TECHNICAL_ERR_MSG = "Something went wrong and we couldn't process your request.";

	private static final Logger logger = Logger.getLogger(CorporateAutoBookingService.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired private B2BExpressCardsServiceClient b2BExpressCardsServiceClient;
	@Autowired private HttpService httpService;
	@Autowired private LobValidatePaymentService lobValidatePaymentService;
	@Autowired private PropertyManager propertyManager;
	@Autowired private CrpAutoBookPayOpService crpAutoBookPayOpService;
	@Autowired private LobDetailsService lobDetailsService;

	public CorporateAutoBookingResponse autoBooking(CorporateAutoBookingRequest request){
		//1. validate request parameters
		ValidationResponse validationResponse = validateRequestParameters(request);
		HashMap<String,String> cardAccessParams = null;
		if( ! validationResponse.isValid()){
			return new CorporateAutoBookingResponse(validationResponse.getErrorMessage(),TECHNICAL_ERR_MSG);
		}


		LobDetailRequest lobDetailRQ = new LobDetailRequest(request.getSuperPnr(), null, request.getUuid(), request.getProduct(), request.getSsoToken(),	null );
		try{
			LobDetailResponse lobDetailRS = lobDetailsService.fetchItineraryDetailsFromLob(lobDetailRQ);
			if(lobDetailRS!=null) {
				String promoCode = lobDetailRS.getPromoCode();
				cardAccessParams = lobDetailRS.getCrpInfoNode();
				request.setPromoCode(promoCode);
				request.setCorporateTripId(lobDetailRS.getCorporateTripId());
			}
		}
		catch(Exception e){
			logger.info("Exception in GetBookingDetails Response, hence not forwarding promo information", e);
		}

		//2.get-payment-option Api (s2s call)
		JSONObject corporatePaymentJson = null;
		String getpayopApiStatus = "";
		try {
			corporatePaymentJson = new JSONObject(crpAutoBookPayOpService.getPayOpRes(request.getSsoToken() , request.getTxnId()));
			getpayopApiStatus = corporatePaymentJson.getString("status");
		} catch(Exception e){
			logger.error("Error while getting response from corporate getPaymentOption api ",e);
			return new CorporateAutoBookingResponse(ErrorMessageUtil.ERROR_IN_CORPORATE_GET_PAYMENTOPTION_API,TECHNICAL_ERR_MSG);
		}
		//3. parse payment option response
		List<String> payOpInPriorityOrderList = null;
		if( !"success".equals(getpayopApiStatus) ){
			return new CorporateAutoBookingResponse(getpayopApiStatus + " status received in corporate getPaymentOptions API response",TECHNICAL_ERR_MSG);
		}else{
			payOpInPriorityOrderList = crpAutoBookPayOpService.getPayOpsInpriorityOrder(corporatePaymentJson,
					request.getBookingType(),request.getProductType());
			if(payOpInPriorityOrderList.size() == 0){
				return new CorporateAutoBookingResponse(ErrorMessageUtil.ERROR_NO_PAYMENT_OPTION_FOUND_WITH_AUTOBOOKPRIORITY_NODE,TECHNICAL_ERR_MSG);
			}
		}
		//4. loop
		CorporateAutoBookingResponse corporateAutoBookingResponse = new CorporateAutoBookingResponse();
		List<String> ppErrCodeList = new ArrayList<String>();
		for( int i=0 ; i < payOpInPriorityOrderList.size() ; i++){
			String paymentOption = payOpInPriorityOrderList.get(i);
			int priority = i+1;
			String cardId = null;
			//4.1 if payment option is corpCard then get cardId from b2BExpressCardsService
			if(PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD.equals(paymentOption)){	
				cardId = getCardIdFromCardService(request, corporateAutoBookingResponse, payOpInPriorityOrderList, cardAccessParams);
				if(cardId == null){
					continue;
				}
			}
			//4.2 validate with lob for getting amount and ttid
			LobValidationResponse lobValidationResponse = lobValidatePaymentService.serverSideValidateWithLob(request, paymentOption);
			//4.3 validation response case-> Failure
			if(lobValidationResponse == null || lobValidationResponse.getResponseStatus() == com.yatra.payment.client.enums.ResponseStatus.FAILURE){
				corporateAutoBookingResponse.getPaymentMethodErrors().put(paymentOption, ErrorMessageUtil.ERROR_LOB_VALIDATION_FAILED);
			}else{ // 4.4 validation response case-> SUCCESS
				String amount = lobValidationResponse.getAmountToCollect();
				String ttid = lobValidationResponse.getTtid();

				//4.4.1 hit payment portal with required parameters
				String responseJSON = null;
				try{
					Map<String,String> paymentPortalParamterMap = prepareMapForPaymentPortal(request, paymentOption, amount, 
							ttid, cardId);
					responseJSON = sendRequestToPaymentPortal(paymentPortalParamterMap);
				}catch(Exception ex){
					logger.error("Exception while getting response from payment portal for super pnr : "+request.getSuperPnr(),ex);
					corporateAutoBookingResponse.setMsg(ErrorMessageUtil.ERROR_WHILE_CONNECTING_TO_PAYMENT_PORTAL);
					corporateAutoBookingResponse.setDisplayMsg(TECHNICAL_ERR_MSG);
					return corporateAutoBookingResponse;
				}
				//4.4.2 parse payment portal response
				Map<String,String> paymentPortalResponseMap = null;
				try {
					paymentPortalResponseMap = objectMapper.readValue(responseJSON, new TypeReference<Map<String,String>>(){});
				} catch (IOException e) {
					logger.error("Exception while parsing payment portal response",e);
					corporateAutoBookingResponse.setMsg(ErrorMessageUtil.ERROR_WHILE_PARSING_PAYMENT_PORTAL_RESPONSE);
					corporateAutoBookingResponse.setDisplayMsg(TECHNICAL_ERR_MSG);
					return corporateAutoBookingResponse;
				}
				// 4.4.3 payment portal response : SUCCESS
				if("true".equals(paymentPortalResponseMap.get("suc"))){
					return getSuccessfulBookingResponse(request, corporateAutoBookingResponse, paymentPortalResponseMap, paymentOption, priority, ttid, cardId,amount);
					// 4.4.4 payment portal response : FAILURE
				}else{ 
					String errormsg = paymentPortalResponseMap.get("errmsg");
					corporateAutoBookingResponse.getPaymentMethodErrors().put(paymentOption, errormsg);
					String errcode = paymentPortalResponseMap.get("errCode");
					ppErrCodeList.add(errcode);
				}
			}
		}
		// if payment failed from both payment options
		corporateAutoBookingResponse.setMsg(ErrorMessageUtil.ERROR_TRANSACTION_FAILED);
		addDisplayMsgToResponse(corporateAutoBookingResponse, ppErrCodeList);
		return corporateAutoBookingResponse;

	}

	private String getPaymentPortalAutoBookingUrl(){
		return "http://payment.service/checkout/pay/crpAutoPay";
	}

	private ValidationResponse validateRequestParameters(CorporateAutoBookingRequest request){
		String errorMessage ="";
		if(StringUtils.isBlank(request.getProduct())){
			errorMessage += "product ,";
		}
		if(StringUtils.isBlank(request.getSuperPnr())){
			errorMessage += "superPnr ,";
		}
		if(StringUtils.isBlank(request.getSsoToken())){
			errorMessage += "ssoToken ,";
		}
		if(StringUtils.isBlank(request.getEmail())){
			errorMessage += "email ,";
		}
		if(StringUtils.isBlank(request.getProductType())){
			errorMessage += "productType ,";
		}
		if(StringUtils.isBlank(request.getBookingType())){
			errorMessage += "bookingType ,";
		}

		ValidationResponse validationResponse = new ValidationResponse();
		validationResponse.setSuperPnr(request.getSuperPnr());
		if("".equals(errorMessage)){
			validationResponse.setValid(true);
			validationResponse.setErrorMessage(errorMessage);
		}else{
			validationResponse.setValid(false);
			validationResponse.setErrorMessage("Required request parameter missing -> " + errorMessage);
		}
		return validationResponse;
	}

	private Map<String,String> prepareMapForPaymentPortal(CorporateAutoBookingRequest request, String payOp,String amount,String ttid,String cardId){
		Map<String,String> map = new HashMap<>();
		map.put(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL, request.getSuperPnr());
		map.put(PaymentUIUtil.MERCHANT_CODE_KEY_FOR_PAYMENT_PORTAL, request.getMerchant());
		map.put(PaymentUIUtil.PRODUCT_CODE_KEY_FOR_PAYMENT_PORTAL, request.getProduct());
		map.put(PaymentUIUtil.EMAIL, request.getEmail());
		map.put(PaymentUIUtil.SSO_TOKEN, request.getSsoToken());
		map.put(PaymentUIUtil.TTID, ttid);
		map.put(PaymentUIUtil.AMOUNT,amount);
		map.put(PaymentUIUtil.PAYMENT_OPTION, payOp);
		map.put(PaymentUIUtil.TXN_ID,request.getTxnId());
		if(StringUtils.isNotBlank(cardId)){
			map.put(PaymentUIUtil.CARD_ID, cardId);
		}
		if("cp".equals(payOp)){
			map.put(PaymentUIUtil.POOL_TYPE, "creditPool");
			map.put(PaymentUIUtil.CORPORATE_TRIP_ID, request.getCorprateTripId());
		}
		return map;
	}

	private CorporateAutoBookingResponse getSuccessfulBookingResponse(CorporateAutoBookingRequest request,
			CorporateAutoBookingResponse corporateAutoBookingResponse,Map<String,String> paymentPortalResponseMap,
			String paymentOption,int priority,String ttid,String cardId,String amount){
		corporateAutoBookingResponse.setStatus(true);
		corporateAutoBookingResponse.setMsg("Transaction successful");
		Map<String,Object> paramsMap= corporateAutoBookingResponse.getParams();
		paramsMap.put(PaymentUIUtil.SUPER_PNR, request.getSuperPnr());
		paramsMap.put(PaymentUIUtil.TTID, ttid);
		paramsMap.put(PaymentUIUtil.MERCHANT_TXN_ID, paymentPortalResponseMap.get(PaymentUIUtil.MERCHANT_TXN_ID));
		paramsMap.put(PaymentUIUtil.AMOUNT, amount);
		Map<String,String> paymentMethodMap = new HashMap<>();
		paymentMethodMap.put(PaymentUIUtil.PAYMENT_OPTION, paymentOption);
		paymentMethodMap.put("priority", String.valueOf(priority));
		if(PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD.equalsIgnoreCase(paymentOption)){
			paymentMethodMap.put(PaymentUIUtil.CARD_ID, cardId);
		}
		paramsMap.put(PaymentUIUtil.PAYMENT_METHOD, paymentMethodMap);
		return corporateAutoBookingResponse;
	}

	private String getCardIdFromCardService(CorporateAutoBookingRequest request,CorporateAutoBookingResponse corporateAutoBookingResponse,
			List<String> payOpInPriorityOrderList , HashMap<String,String> cardAccessParams){
		CorporateCardsDisplayResponse corporateCardsDisplayResponse = null;
		String cardId = null;
		try{
			corporateCardsDisplayResponse = b2BExpressCardsServiceClient.getSsoCardsForDisplay(request.getSsoToken(), cardAccessParams);

		}catch(Exception e){
			logger.error("Exception while getting corporate card from b2BExpressCardsService for super pnr : "+request.getSuperPnr(),e);
			corporateAutoBookingResponse.getPaymentMethodErrors().put(PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD,
					"Unable to get response from  B2B Express Cards Service");
			return null;
		}
		ResponseStatus responseStatus = corporateCardsDisplayResponse.getResponse();
		if("SUCCESS".equals(responseStatus.getStatus())){
			cardId = corporateCardsDisplayResponse.getCorporateCards().get(0).getCardId();
		}else{
			corporateAutoBookingResponse.getPaymentMethodErrors().put(PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD,
					"Failure Response received from  B2B Express Cards Service ,Error Message ="+responseStatus.getErrorMessage());
		}

		return cardId;
	}



	private String sendRequestToPaymentPortal(Map<String,String> paymentPortalParamterMap) throws IOException{
		String responseJSON = null;
		if (propertyManager.getPropertyAsBoolean("dummy.booking.detail.api")) {
			responseJSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/autoBookingPaymentPortalResponse.txt"));
		}else{
			String paymentPortalUrl = getPaymentPortalAutoBookingUrl();
			HttpEndPoint endPoint = new HttpEndPoint(paymentPortalUrl, "GET");
			logger.info("request sent to payment portal , request  paramters :"+paymentPortalParamterMap);
			responseJSON = httpService.invoke(endPoint, paymentPortalParamterMap);
			logger.info("response received from  payment portal , response  :"+responseJSON);
		}
		return responseJSON;
	}

	private void addDisplayMsgToResponse(CorporateAutoBookingResponse response,List<String> ppErrCodeList){
		if(ppErrCodeList.contains(INSUFFICIENT_CREDIT_POOL_BALANCE_CODE)){
			response.setDisplayMsg(INSUFFICIENT_CREDIT_POOL_BALANCE_MSG);
		}else{
			response.setDisplayMsg(TECHNICAL_ERR_MSG);
		}

	}
}
