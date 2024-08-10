package com.yatra.payment.ui.helper;

import static com.yatra.payment.ui.enums.FailureCode.FETCH_AGENT_PAYMENT_OPTIONS_INVALID_RESPONSE_CODE;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_CRP_PAYMENT_OPTIONS_BOOKING_TYPE_NOT_AVAILABLE;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_CRP_PAYMENT_OPTIONS_NOT_AVAILABLE;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yatra.payment.client.service.WalletService;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.payswift.beans.PaymentRequest;
import com.yatra.payment.ui.dao.ApiInfoDAO;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.payment.ui.service.impl.CurrencyConversionServiceImpl;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class PaymentUIHelper {
	private static Logger logger = Logger.getLogger(PaymentUIHelper.class);

	@Autowired private PayswiftStagesDAOImpl paymentStagesDao;
	@Autowired private WalletService walletService;
	@Autowired private AgentProfileService agentProfileService;
	@Autowired ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired private ApiInfoDAO apiInfoDAO;
	@Autowired private CurrencyConversionServiceImpl currencyConversionService;
	
	private String skipOtpFlagPayments;

	@PostConstruct
	public void setSkipOtpFlagPayments() {
		setSkipOtpFlagPayments(paymentStagesDao.getSkipOtpFlagPayments());
	}

	public String getSkipOtpFlagPayments() {
		return skipOtpFlagPayments;
	}

	public void setSkipOtpFlagPayments(String skipOtpFlagPayments) {
		this.skipOtpFlagPayments = skipOtpFlagPayments;
	}

	public boolean isFirstPaymentAttempt(String superPNR){
		String params = paymentStagesDao.fetchParamForSuperPNR(superPNR ,"PAY_NOW_RES");

		if(StringUtils.isEmpty(params))
			return true;
		else
			return false;
	}

	public long getPreviousRedeemedEcash(String superPNR, String email){

		if(StringUtils.isEmpty(superPNR) && StringUtils.isEmpty(email)){
			logger.debug("SuperPNR or Email is Empty... Returning 0 as default");
			return 0;
		} else if (isFirstPaymentAttempt(superPNR)){
			logger.debug("First payment attempt... Returning 0 as default");
			return 0;
		} else{
			logger.debug("User returned to Payswift again... Fetching previous redeemed amount from WalletService");
			return walletService.getPrevRedeemedEcash(email, superPNR);
		}
	}

	public Map<String, String> getSkipOtpFlagMap(JSONObject agentPaymentOptionJSON, String bookingType, boolean isCrpProduct, String entity, String product, boolean passthrough) {
		if (isCrpProduct) {
			return skipOtpFlagForCrp(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
		}
		return skipOtpFlagForB2B(agentPaymentOptionJSON);
	}

	private Map<String, String> skipOtpFlagForCrp(JSONObject agentPaymentOptionJSON, String bookingType, String entity, String product, boolean passthrough) {
		JSONObject paymentOptionsByBookingType = crpPaymentOptionsByBookingType(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
		Map<String, String> skipOtpFlagsByPayOps = new HashMap<>();

		for (Object o : paymentOptionsByBookingType.keySet()) {
			String paymentOption = (String) o;
			logger.info("Getting skipOtp flag for: " + paymentOption);
			JSONObject perPaymentOptionJson = paymentOptionsByBookingType.getJSONObject(paymentOption);
			if (perPaymentOptionJson.has("skipOTP")) {
				String skipOTP = perPaymentOptionJson.get("skipOTP").toString();
				skipOtpFlagsByPayOps.put(paymentOption.toLowerCase(), skipOTP);
				logger.info("Got skipOTP flag as: " + skipOTP + ", for payment option: " + paymentOption);
			}
			else {
				skipOtpFlagsByPayOps.put(paymentOption.toLowerCase(), "false");
				logger.info("Did not get skipOTP flag for payment option: " + paymentOption + ", hence setting it as false");
			}
		}
		logger.debug("SKIP_OTP_FLAG_MAP: " + skipOtpFlagsByPayOps);
		return skipOtpFlagsByPayOps;
	}

	private Map<String, String> skipOtpFlagForB2B(JSONObject agentPaymentOptionJSON) {
		String skipOtpFlag;
		Map<String, String> skipOtpFlagsByPayOps = new HashMap<>();
		if(agentPaymentOptionJSON.getInt("resCode") != 200){
			throw new PayswiftException(FETCH_AGENT_PAYMENT_OPTIONS_INVALID_RESPONSE_CODE.getCode(), "Invalid response code for get agent payment options request");
		}
		if(agentPaymentOptionJSON.containsKey(PaymentUIUtil.SKIP_OTP_LOB_FLAG)) {
			skipOtpFlag = agentPaymentOptionJSON.getString(PaymentUIUtil.SKIP_OTP_LOB_FLAG);
			logger.info("Got skipOtpForCC flag as " + agentPaymentOptionJSON.getString(PaymentUIUtil.SKIP_OTP_LOB_FLAG) + " from LOB");
			skipOtpFlagsByPayOps.put("cc", skipOtpFlag);
			skipOtpFlagsByPayOps.put("dc", skipOtpFlag);
		}
		else {
			logger.info("Did not get skipOtpForCC flag from LOB, hence setting it as false");
			skipOtpFlagsByPayOps.put("cc", "false");
			skipOtpFlagsByPayOps.put("dc", "false");
		}
		logger.debug("SKIP_OTP_FLAG_MAP: " + skipOtpFlagsByPayOps);
		return skipOtpFlagsByPayOps;
	}
	
	public void skipOtpFlagForB2C(PaymentRequest paymentRequest) {
		Map<String, String> skipOtpFlagsByPayOps = new HashMap<>();
		if(paymentRequest.getSkipOtpFlagMap()==null) {
			logger.debug("SKIP_OTP_FLAG_MAP: " + skipOtpFlagsByPayOps);
			logger.info("Did not get skipOtpForCC flag from LOB, hence setting it as false");
			skipOtpFlagsByPayOps.put("cc", "false");
			skipOtpFlagsByPayOps.put("dc", "false");
			logger.debug("SKIP_OTP_FLAG_MAP: " + skipOtpFlagsByPayOps);
			paymentRequest.setSkipOtpFlagMap(skipOtpFlagsByPayOps);
		}		
	}

	public void clearLists() {
		if(skipOtpFlagPayments != null){
			logger.info("Clearing List PaymentUIHelper.skipOtpFlagPayments");
			skipOtpFlagPayments = null;
		}
		logger.info("Clearing currency related cache");
		currencyConversionService.clearCurrencyList();
	}

	public JSONObject crpPaymentOptionsByBookingType(JSONObject agentPaymentOptionJSON, String bookingType, String entity, String product, boolean passthrough) {
		JSONObject payOps = new JSONObject();
		if(agentPaymentOptionJSON.has("paymentOptions")) {
			JSONObject paymentOptions = agentPaymentOptionJSON.getJSONObject("paymentOptions");
			if(paymentOptions.has(bookingType)) {
				JSONArray paymentOptionsJSONArray = paymentOptions.getJSONArray(bookingType);
				for(int index = 0; index < paymentOptionsJSONArray.size(); index++) {
					JSONObject paymentOptionsJSONArrayJSONObject = paymentOptionsJSONArray.getJSONObject(index);
					for(Iterator iterator = paymentOptionsJSONArrayJSONObject.keySet().iterator();iterator.hasNext();) {
						String payopKey = (String)iterator.next();
						JSONObject payopValue = paymentOptionsJSONArrayJSONObject.getJSONObject(payopKey);
						payOps.put(payopKey, payopValue);
					}
				}
				//Putting hardcoded UPI payop for all Corporate tenants except GDS flow and ATB flows
				if(!isProductGDSHotel(product, passthrough) && "PERSONAL".equalsIgnoreCase(bookingType))
					payOps.put("UPI", JSONObject.fromObject("{\"skipOTP\":false,\"showStoredCards\":true}"));
				return payOps;
			}
			throw new PayswiftException(FETCH_CRP_PAYMENT_OPTIONS_BOOKING_TYPE_NOT_AVAILABLE.getCode(), "bookingtype: " + bookingType + " json not available in paymentOptions json");
		}
		throw new PayswiftException(FETCH_CRP_PAYMENT_OPTIONS_NOT_AVAILABLE.getCode(), "paymentOptions json not available in agentPaymentOptions ");
	}
	
	public String getBookingType(String agentProfileType) {
		if ("PERSONAL".equalsIgnoreCase(agentProfileType)) {
			return agentProfileType.toLowerCase();
		}
		return "official";
	}

	public void logIntoDB(String params, String stage,String superPnr, String apiName) {
		threadPoolTaskExecutor.submit(new Runnable() {
			@Override
			public void run() {
				apiInfoDAO.logIntoAPIInfoAudit(superPnr, apiName, params, stage);
			}
		});

	}
	
	public JSONObject getCVVSupportedCardTypeJSON(JSONObject paymentOptionJSON) throws JSONException {
		JSONObject withoutCVVSupportedCardTypeJson = new JSONObject();
		for(Iterator iterator = paymentOptionJSON.keySet().iterator();iterator.hasNext();) {
			String key = (String)iterator.next();
			if(!(key.equalsIgnoreCase("cc") || key.equalsIgnoreCase("dc")))
				continue;
			else {
				withoutCVVSupportedCardTypeJson.put(key, new JSONObject());
				JSONArray cardTypesJsonArray = paymentOptionJSON.getJSONObject(key).getJSONArray("cardTypes");
				for(int i = 0; i < cardTypesJsonArray.size(); i++) {
					JSONObject cardType = cardTypesJsonArray.getJSONObject(i);
					withoutCVVSupportedCardTypeJson.getJSONObject(key).put(cardType.getString("code"), cardType.getJSONObject("cardDetail").getString("withoutCVVSupported"));
				}
			}
		}
		return withoutCVVSupportedCardTypeJson;
	}

	public boolean validateSkipOtp(String superPnr, String paymentOption, String cardType, String bin, String product, boolean isPassthrough) {
		if(isProductGDSHotel(product, isPassthrough))
			return true;
		
		String response = apiInfoDAO.getSkipOtpResponseForSuperPnr(superPnr);
		String skiOtpBins = apiInfoDAO.getSkipOtpBinsForSuperPnr(superPnr);
		JSONObject withoutCVVJson = JSONObject.fromObject(response);
		JSONObject skipOtpJson = JSONObject.fromObject(skiOtpBins);
		String sixDigitBin = bin;
		if(StringUtils.isNotBlank(bin) && bin.length() > 6)
		{
			sixDigitBin = bin.substring(0,6);
		}
		
		if("true".equalsIgnoreCase(withoutCVVJson.getJSONObject(paymentOption).getString(cardType))
				&& (skipOtpJson.getJSONArray("binsForSkipOtp").size() > 0 &&
				(skipOtpJson.getJSONArray("binsForSkipOtp").contains(bin) || skipOtpJson.getJSONArray("binsForSkipOtp").contains(sixDigitBin))))
			return true;
		else return false;
	}
	
	private boolean isProductGDSHotel(String product, boolean passThrough) {
        if (passThrough) {
            if (PaymentUIUtil.PRODUCT_CORP_APP_DOM_HOTEL_ANDROID.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_APP_INT_HOTEL_ANDROID.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_APP_DOM_HOTEL_IOS.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_APP_INT_HOTEL_IOS.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_DOM_HOTEL.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_INT_HOTEL.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_INT_HOTEL_NEW.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_DOM_HOTEL_NEW.equalsIgnoreCase(product))
                return true;
        }
        return false;
    }
	
	public  org.json.JSONObject getExchangeRates(String merchant, String product, String superPnr) {
		try{
			org.json.JSONObject requestJson = currencyConversionService.getCurrencyConversionAPIRequest(product, merchant, superPnr);
			return currencyConversionService.getRequiredData(requestJson, null);
		} catch(Exception ex) {
			logger.error("Error while getting currenct conversion for superPNR : " + superPnr + " , Exception is : ", ex);
			return currencyConversionService.getErrorJson(CurrencyConversionServiceImpl.GENERIC_ERROR_MESSAGE);
		}
	}
	
	public void makeS2SCallIfEligible(PayNowRequest payNowRequest, PayNowResponse payNowResponse, Map<String, String> resultMap, boolean txnStatus) {

		String response = null;
		String status = null;
		boolean isEligible = false;
		isEligible = PaymentUIUtil.checkS2sEligibility(payNowResponse, txnStatus);
		if(isEligible) {
			logger.info("Eligible for S2S confirmation call, hence making the same");
			String s2surlString = PaymentUIUtil.getUrlString(payNowResponse.getValidationResponse().getS2Surl(), resultMap);
			s2surlString = PaymentUIUtil.appendSsoTokenInS2SCall(s2surlString,payNowRequest.getSSOToken());
			try {
				response = PaymentUIUtil.sendRequest(s2surlString,"GET");
				logger.info("Server to Server Response received from LOB : " + response);
			}
			catch (Exception e) {
				logger.info("Error making Server to Server call ", e);
			}
			if(!YatraUtil.isNullOrEmpty(response)) {
				try {
					org.json.JSONObject respJson = new org.json.JSONObject(response);
					status = respJson.getString("status");
					logger.info("Status at LOB end :" + status);
				}
				catch(Exception e) {
					logger.info("Error while parsing response from LOB ", e);	
				}
			}
			paymentStagesDao.createS2SDBEntry(payNowResponse.getTtid(), resultMap.get("mtxnid"), payNowRequest.getProductCode(), s2surlString, response, status);
		}
		else {
			logger.info("Not Eligible for S2S confirmation call, hence not making the same");
		}
	}
	
}
