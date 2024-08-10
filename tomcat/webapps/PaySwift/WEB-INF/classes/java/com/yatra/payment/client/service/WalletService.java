package com.yatra.payment.client.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.client.beans.WalletRequest;
import com.yatra.payment.client.service.impl.PaymentDivService;
import com.yatra.payment.client.utils.PaymentClientUtil;
import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.WalletAuthResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.wallet.beans.req.GetRedeemedECashRQ;
import com.yatra.wallet.beans.resp.GetRedeemedECashRS;
import com.yatra.wallet.client.WalletClient;
import com.yatra.wallet.enums.RedeemedState;

@Service("walletService")
public class WalletService {
	
	private Logger logger = Logger.getLogger(WalletService.class);
	
	@Autowired private PaymentDivService paymentWebDivService;
	@Autowired private HttpUtil httpUtil;
	@Autowired private PropertyManager propertyManager;
	
	/*
	*Now LOBs who want to validate that charged amount is within the limit of YLPMAX and YLPMAX has not been tempered from UI side ,
	*need to pass two more parameters
	*validate=true and YLPMax while using getWalletInfo API .
	*/
	public WalletInfo getWalletInfo(String superPNR, String paymentParams,Boolean validate, String YLPMax) throws IllegalArgumentException {
		
		logger.info("Initiating getWalletInfo for superPNR : " + superPNR);
		
		WalletInfo walletInfo = null;
		try {
			// Throw exception if either of the arguments is null or empty.
			if(StringUtils.isEmpty(paymentParams) || StringUtils.isEmpty(superPNR)) {
				logger.error("Wallet Info cannot be fetched. Request parameters cannot be null or empty. Super PNR : " + superPNR);
				throw new IllegalArgumentException("Wallet Info cannot be fetched. Request parameters cannot be null or empty");
			}
	 		
			// Get walletId.
			String walletId = getWalletId(paymentParams);
			logger.info("WalletId for superPNR : " + superPNR + " is : " + walletId);
			
			// If walletId is null, no wallet payment was initiated. Return null.
			if(StringUtils.isEmpty(walletId))
				return null;
			
			return getWalletInfoByWalletID(superPNR, walletId, validate, YLPMax);
			
		} catch (Exception e) {
			logger.error("Exception occurred while getting walletInfo for super PNR : " + superPNR, e);
			throw new PayswiftException(e);
		}
   	}
	
	public WalletInfo getWalletInfoByWalletID(String superPNR, String walletId, boolean validate, String ylpmax) throws Exception {
		// Create request map to fetch walletInfo.
		Map<String, String> requestMap = createWalletInfoRequestMap(superPNR, walletId, validate, ylpmax);
				
		// Get request data to be posted.
		String postData = httpUtil.createPostDataFromMap(requestMap);
		logger.info("Get WalletInfo request post data for superPNR : " + superPNR + " and walletId : " + walletId + " is : " + postData);
			
		// Get URL.
		String url = getWalletInfoURL();
				
		// Send post request on URL with post data to fetch walletInfo.
		String responseJSON = httpUtil.invoke(url, postData, "GET_WALLET_INFO");
		logger.info("Get WalletInfo response for superPNR : " + superPNR + " and walletId : " + walletId + " is : " + responseJSON);
					
		// Create WalletInfo bean from response JSON.
		return createWalletInfoBean(responseJSON, walletId, superPNR);
	}
	
	public boolean capture(WalletRequest walletRequest) {
		
		logger.info("Initiating wallet capture for walletRequest : " + walletRequest);
		boolean status = false;
		try {
			// Throw exception if walletRequest is null.
			if(walletRequest == null) {
				logger.error("Wallet Payment Could not be completed (cap). WalletRequest is null : " + walletRequest);
				throw new IllegalArgumentException("Wallet Payment Could not be completed (cap). WalletRequest is null : " + walletRequest);
			}
			
			// Get request parameters from WalletRequest 
			String superPNR = walletRequest.getSuperPNR();
			String walletId = walletRequest.getWalletId();
			String userEmail = walletRequest.getUserEmail();
			String amountInRupees = walletRequest.getAmountInRupees();
			String merchantCode = walletRequest.getMerchantCode();
			String productCode = walletRequest.getProductCode();
			String actualTtid = walletRequest.getTtid();
			
			// If either of the request parameters is null, request is invalid, throw exception.
			boolean isRequestInValid = StringUtils.isEmpty(superPNR) || StringUtils.isEmpty(walletId) || StringUtils.isEmpty(userEmail) || StringUtils.isEmpty(amountInRupees) || StringUtils.isEmpty(merchantCode) || StringUtils.isEmpty(productCode);
			if(isRequestInValid) {
				logger.error("Wallet Payment Could not be completed (cap). One or more paremeters is/are null : " + walletRequest);
				throw new IllegalArgumentException("Wallet Payment Could not be completed (cap). One or more paremeters is/are null : " + walletRequest);
			}
			
			// Create request map to capture eWallet payment
			Map<String, String> requestMap = createCaptureRequestMap(superPNR, walletId, userEmail, amountInRupees, merchantCode, productCode, actualTtid);
			
			// Get request data to be posted.
			String postData = httpUtil.createPostDataFromMap(requestMap);
			logger.info("Wallet Capture request post data for superPNR : " + superPNR + " and walletId : " + walletId + " is : " + postData);
			
			// Get URL
			String url = getCapURL();
			
			// Send post request on URL with post data to capture eWallet Payment.
			String responseJSON = httpUtil.invoke(url, postData, "WALLET_CAPTURE");
			logger.info("Wallet Capture response for superPNR : " + superPNR + " and walletId : " + walletId + " is : " + responseJSON);
			
			// Get capture response status from responseJSON
			status = getStausFromJSON(responseJSON, walletRequest);
			
		} catch (Exception e) {
			logger.error("Exception occurred while capturing wallet payment with wallet request : " + walletRequest, e);
			status = false;
		}
		logger.info("Returning capture status : " + status + " for walletRequest : " + walletRequest);
		return status;
	}
	
	public boolean reverseAuth(WalletRequest walletRequest) {
		
		logger.info("Initiating wallet reverse auth for walletRequest : " + walletRequest);
		
		boolean status = false;
		try {
			// Throw exception if WalletRequest is null.
			if(walletRequest == null) {
				logger.error("Wallet Payment Could not be cancelled (auth rev). WalletRequest is null : " + walletRequest);
				throw new IllegalArgumentException("Wallet Payment Could not be cancelled (auth rev). WalletRequest is null : " + walletRequest);
			}
			
			// Get request parameters from WalletRequest
			String superPNR = walletRequest.getSuperPNR();
			String walletId = walletRequest.getWalletId();
			String userEmail = walletRequest.getUserEmail();
			String amountInRupees = walletRequest.getAmountInRupees();
			String merchantCode = walletRequest.getMerchantCode();
			String productCode = walletRequest.getProductCode();
			
			// If either of the request parameters is null, request is invalid, throw exception.
			boolean isRequestInValid = StringUtils.isEmpty(superPNR) || StringUtils.isEmpty(walletId) || StringUtils.isEmpty(userEmail) || StringUtils.isEmpty(amountInRupees) || StringUtils.isEmpty(merchantCode) || StringUtils.isEmpty(productCode);
			if(isRequestInValid) {
				logger.error("Wallet Payment Could not be cancelled (auth rev). One or more paremeters is/are null : " + walletRequest);
				throw new IllegalArgumentException("Wallet Payment Could not be cancelled (auth rev). One or more paremeters is/are null : " + walletRequest);
			}
			
			// Create request map to reverse auth eWallet payment
			Map<String, String> requestMap = createAuthRevRequestMap(superPNR, walletId, userEmail, amountInRupees, merchantCode, productCode);
			
			// Get request data to be posted.
			String postData = httpUtil.createPostDataFromMap(requestMap);
			logger.info("Wallet Auth Reverse request post data for superPNR : " + superPNR + " and walletId : " + walletId + " is : " + postData);
			
			// Get URL
			String url = getAuthRevURL();
			
			// Send post request on URL with post data to fetch walletInfo.
			String responseJSON = httpUtil.invoke(url, postData, "WALLET_REVERSE_AUTH");
			logger.info("Wallet Auth reverse response for superPNR : " + superPNR + " and walletId : " + walletId + " is : " + responseJSON);
			
			// Get auth reverse response status from responseJSON
			status = getStausFromJSON(responseJSON, walletRequest);
			
		} catch (Exception e) {
			logger.info(FailureCode.WALLET_REVERSE_AUTH_EXCEPTION.getCode() + " Exception occured while making reverse Auth call");
			status = false;
		}
		logger.info("Returning auth reverse status : " + status + " for walletRequest : " + walletRequest);
		return status;
	}
	
	public WalletAuthResponse auth(PayNowRequest payNowRequest) {
		
		logger.info("Initiating wallet auth for walletRequest");
		
		boolean status = false;
		try {
			// Create request map to reverse auth eWallet payment
			Map<String, String> requestMap = createAuthRevRequestMap(payNowRequest);
			
			// Get request data to be posted.
			String postData = httpUtil.createPostDataFromMap(requestMap);
			logger.info("Wallet Auth Reverse request post data for superPNR : " + payNowRequest.getSuperPnr() + " is : " + postData);
			
			// Get URL
			String url = propertyManager.getProperty("non.secure.wallet.auth.url");
			Long requestTime = System.currentTimeMillis();
			
			// Send post request on URL with post data to fetch walletInfo.
			String response = httpUtil.invoke(url, postData, "WALLET_AUTH");
			
			Long responseTime = System.currentTimeMillis();
			Long responseTimeInMilliSeconds = (responseTime - requestTime);
			logger.info("Wallet Auth response took " + responseTimeInMilliSeconds+" ms for superPNR : " + payNowRequest.getSuperPnr() + " and response : " + response);
			
			return  constructValidationResponse(response);
			
		} catch (Exception e) {
			logger.error("Exception occurred while amking wallet auth ", e);
			throw new PayswiftException(FailureCode.WALLET_AUTH_FAILED.getCode(), "Wallet Auth Failed", e);
		}
	}

	public Long getPrevRedeemedEcash(String email, String superPNR){
		try{
			logger .debug("Hitting Wallet Service to fetch RedeemedEcash for Email : " + email + " and superPNR : " + superPNR);
			GetRedeemedECashRQ getRedeemedECashRQ = new GetRedeemedECashRQ();

			getRedeemedECashRQ.setState(RedeemedState.REVERSED);
			getRedeemedECashRQ.setUserEmail(email);
			getRedeemedECashRQ.setBookingId(superPNR);

			GetRedeemedECashRS getRedeemedECashRS = new WalletClient().getRedeemedECash(getRedeemedECashRQ);

			if (getRedeemedECashRS != null && getRedeemedECashRS.isStatus()) {
				 Long amountRedeemedinRs =  getRedeemedECashRS.getAmountInPaisa()/100 ;
				 logger.debug("Previous redeemed amount received = " + amountRedeemedinRs + " for Email " + email + " and superPNR " + superPNR);
				 return amountRedeemedinRs;
			}
			logger.error("getPrevRedeemedEcash either null or got failure in response for Email : " + email + " and superPNR : " + superPNR);
			
		}	catch(Exception ex){
			logger.error("Exception in getting Previous Redeemed Ecash for Email : " + email + " and superPNR : " + superPNR, ex);
		}
		return new Long(0);
	}

	private String getWalletId(String paymentParams) {
		try {
			Map<String, String> paramsMap = paymentWebDivService.getPaymentParamsMap(paymentParams);
			return paramsMap.get(PaymentConstants.WALLET_ID);
		} catch (Exception e) {
			logger.error("Exception occurred while getting walletId from paymentParams string", e);
			return null;
		}
	}
	
	private Map<String, String> createWalletInfoRequestMap(String superPNR, String walletId,Boolean validate,String YLPMax) {
		Map<String, String> requestMap = new HashMap<String, String>();
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.TRIP_ID, superPNR);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.WALLET_ID, walletId);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.VALIDATE, validate.toString());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.YLPMAX, YLPMax);
		return requestMap;
	}
	
	private String getWalletInfoURL() {
		String secureURL = propertyManager.getProperty("secure.wallet.info.url");
		String nonSecureURL = propertyManager.getProperty("non.secure.wallet.info.url");
		return PaymentClientUtil.getRequestURL(secureURL, nonSecureURL);
	}
	
	private WalletInfo createWalletInfoBean(String responseJSON, String walletId, String superPNR) {
		WalletInfo walletInfo = null;
		try {
			if(StringUtils.isEmpty(responseJSON)) {
				logger.error("WalletInfo Response JSON is null/empty : " + responseJSON + " for super PNR : " + superPNR + " and walletId : " + walletId);
				return null;
			}
			JSONObject jsonObject = new JSONObject(responseJSON);

				String amount = String.valueOf(jsonObject.get("amount"));
				walletInfo = new WalletInfo();
				walletInfo.setAmountInRupees(amount);
				walletInfo.setWalletId(walletId);
				walletInfo.setStatus(String.valueOf(jsonObject.get("status")));
				try{
					walletInfo.setMessage(String.valueOf(jsonObject.get("message")));
				}catch(JSONException jsonException){
					// do nothing as message is not mandatory .
					logger.error("message object not found in JSONObject . ");
				}
		} catch (Exception e) {
			logger.error("Exception occurred while creating WalletInfo bean from JSON : " + responseJSON + " for super PNR : " + superPNR + " and walletId : " + walletId, e);
		}
		logger.info("WalletInfo for superPNR : " + superPNR + " is : " + walletInfo);
		return walletInfo;
	}
	
	private boolean getStausFromJSON(String responseJSON, WalletRequest walletRequest) {
		try {
			if(StringUtils.isEmpty(responseJSON)) {
				logger.error("Response JSON is null/empty for cap or auth wallet payment : " + responseJSON + " for WalletRequest : " + walletRequest);
				return false;				
			}
			JSONObject jsonObject = new JSONObject(responseJSON);
			return jsonObject.getBoolean("status");
		} catch (Exception e) {
			logger.error("Exception occurred while determining status from response JSON : " + responseJSON + " for WalletRequest : " + walletRequest, e);
		}
		return false;
	}

	private Map<String, String> createCaptureRequestMap(String superPNR, String walletId, String userEmail, String amountInRupees, String merchantCode, String productCode, String actualTtid) {
		Map<String, String> requestMap = new HashMap<String, String>();
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.TRIP_ID, superPNR);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.WALLET_ID, walletId);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.CUSTOMER_EMAIL, userEmail);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.AMOUNT, amountInRupees);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.MERCHANT_CODE, merchantCode);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.PRODUCT_CODE, productCode);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.ACTUAL_TTID, actualTtid);
		return requestMap;
	}
	
	private String getCapURL() {
		String secureURL = propertyManager.getProperty("secure.wallet.cap.url");
		String nonSecureURL = propertyManager.getProperty("non.secure.wallet.cap.url");
		return PaymentClientUtil.getRequestURL(secureURL, nonSecureURL);
	}
	
	private Map<String, String> createAuthRevRequestMap(String superPNR, String walletId, String userEmail, String amountInRupees, String merchantCode, String productCode) {
		Map<String, String> requestMap = new HashMap<String, String>();
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.TRIP_ID, superPNR);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.WALLET_ID, walletId);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.CUSTOMER_EMAIL, userEmail);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.AMOUNT, amountInRupees);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.MERCHANT_CODE, merchantCode);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.PRODUCT_CODE, productCode);
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.ORIGIN, PaymentConstants.ORIGIN_SERVER);
		return requestMap;
	}
	
	private Map<String, String> createAuthRevRequestMap(PayNowRequest payNowRequest) {
		Map<String, String> requestMap = new HashMap<String, String>();
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.TRIP_ID, payNowRequest.getSuperPnr());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.CUSTOMER_EMAIL, StringUtils.isEmpty(payNowRequest.getUserEmail()) ? payNowRequest.getBookingDetails().getUserEmail() : payNowRequest.getUserEmail());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.AMOUNT, payNowRequest.getAmountToRedeem());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.YLPMAX, payNowRequest.getBookingDetails().getMaximumRedeemableECash());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.SSO_TOKEN, payNowRequest.getSSOToken());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.PRODUCT,payNowRequest.getBookingDetails().getWalletProduct());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.CHANNEL,payNowRequest.getBookingDetails().getChannel());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.DEVICE,payNowRequest.getBookingDetails().getDevice());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.MERCHANT_CODE, payNowRequest.getMerchantCode());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.PRODUCT_CODE, payNowRequest.getProductCode());
		PaymentClientUtil.populateMap(requestMap, PaymentConstants.ORIGIN, PaymentConstants.ORIGIN_SERVER);
		return requestMap;
	}
	
	private String getAuthRevURL() {
		String nonSecureURL = propertyManager.getProperty("non.secure.wallet.rev.auth.url");
		return nonSecureURL;
	}
	
	private WalletAuthResponse constructValidationResponse(String response) throws JSONException {
		JSONObject jsonResponse = new JSONObject(response);
		WalletAuthResponse walletAuthResponse = new WalletAuthResponse();
		try{
			walletAuthResponse.setStatus(jsonResponse.optString(PaymentUIUtil.STATUS));
			walletAuthResponse.setAmountInPaisa(jsonResponse.optString(PaymentUIUtil.AMOUNT_IN_PAISA));
			walletAuthResponse.setRedeemedEcash(jsonResponse.optString(PaymentUIUtil.REDEEMED_ECASH));
			walletAuthResponse.setResponseCode(jsonResponse.optString(PaymentUIUtil.RESPONSE_CODE));
			walletAuthResponse.setResponseMsg(jsonResponse.optString(PaymentUIUtil.RESPONSE_MSG));
			walletAuthResponse.setSourceId(jsonResponse.optString(PaymentUIUtil.SOURCE_ID));
			walletAuthResponse.setTransactionId(jsonResponse.optString(PaymentUIUtil.TRANSACTION_ID));
			walletAuthResponse.setWalletId(jsonResponse.optString(PaymentUIUtil.WALLETID));
		}catch(Exception ex){
			logger.error("Exception Occured while using Auth Ecash service , "
					+ "most probably card service is down or is not responding correctly ,"
					+ "Received this from Auth Ecash service ");
		}

		return walletAuthResponse;
	}
	
}
