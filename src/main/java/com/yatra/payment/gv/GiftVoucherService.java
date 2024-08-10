package com.yatra.payment.gv;

import static com.yatra.payment.ui.util.PaymentUIUtil.CONTENT_TYPE_APPLICATION_JSON;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.service.TenantService;
import com.yatra.payment.ui.service.impl.GiftVoucherValidationService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

import net.sf.json.JSONArray;

@Service
public class GiftVoucherService {
	
	private static Logger logger = Logger.getLogger(GiftVoucherService.class);
	private static final String GENERIC_ERROR_MESSAGE = "System error occoured.";
	private static final String PAYOP_NOT_ENABLED = "GiftVoucher is not enabled for the product.";
	private static final String GENERIC_ERROR_DISPLAY_MESSAGE = "Voucher validation failed, Please try again.";
	private static final String SERVICE_NAME_GET_VOUCHER_TYPE = "GV_GET_VOUCHER_TYPE";
	private static final String SERVICE_NAME_CHECK_BALANCE = "GV_CHECK_BALANCE";
	private static final String SERVICE_NAME_VALIDATE_VOUCHER = "GV_VALIDATE_VOUCHER";
	private static final String SERVICE_NAME_UNBLOCK_VOUCHER = "GV_UNBLOCK_VOUCHER";
	private static final String SERVICE_NAME_REDEEM_VOUCHER = "GV_REDEEM_VOUCHER";
	
	private Gson gson = new Gson(); 
	
	@Autowired private StatusProviderService statusProviderService;
	@Autowired private PropertyManager propertyManager;
	@Autowired private HttpService httpService;
	@Autowired private PaymentUIHelper paymentUIHelper;
	@Autowired private TenantService tenantService;
	@Autowired private PayswiftStagesDAOImpl paymentDAO;
	
	public String getType(String voucherCode, String merchant, String product, String superPNR) {
		logger.info("Received " + SERVICE_NAME_GET_VOUCHER_TYPE + " request with parameters, voucherCode : " + voucherCode + ", merchant : " + merchant + ", product : " + product + ", superPNR : " + superPNR);
		try {
			if(paymentDAO.getIsGVFraudCheckEnabled())
				return getErrorJson(PAYOP_NOT_ENABLED, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
			Boolean isGVEnabled = statusProviderService.isPaymentOptionEnabled(merchant, product, PaymentUIUtil.PAYMENT_OPTION_GV);
			if(isGVEnabled == null || !isGVEnabled){
				logger.debug("GV is not enabled for the product : " + product + " and merchnat : " + merchant);
				return getErrorJson(PAYOP_NOT_ENABLED, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
			}
			
			String url = propertyManager.getProperty("gv.service.voucher.type.endpoint");
			logger.debug("Hitting " + SERVICE_NAME_GET_VOUCHER_TYPE + " API for superPNR : " + superPNR + " with URL: " + url + " and voucherCode : " + voucherCode);
			
			HttpEndPoint endPoint = new HttpEndPoint(url, "POST");
			String response = httpService.invoke(endPoint, new HashMap<String, String>(){{put("code", voucherCode);}});
			
			logger.debug(SERVICE_NAME_GET_VOUCHER_TYPE + " response for superPNR : " + superPNR + " is : " + response);
			
			return response;
		} catch(Exception ex) {
			logger.error("Error while GET_VOUCHER_TYPE for voucher : " + voucherCode + " and superPNR : " + superPNR + ", Exception is : ", ex);
			return getErrorJson(GENERIC_ERROR_MESSAGE, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
		}
	}
	
	public String checkBalance(String requestJson, String merchant, String product, String superPNR) {
		logger.info("Received " + SERVICE_NAME_CHECK_BALANCE + " request with parameters, merchant : " + merchant + ", product : " + product + ", superPNR : " + superPNR + ", requestJson : " + requestJson);
		try {
			if(paymentDAO.getIsGVFraudCheckEnabled())
				return getErrorJson(PAYOP_NOT_ENABLED, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
			Boolean isGVEnabled = statusProviderService.isPaymentOptionEnabled(merchant, product, PaymentUIUtil.PAYMENT_OPTION_GV);
			if(isGVEnabled == null || !isGVEnabled){
				logger.debug("GV is not enabled for the product : " + product + " and merchant : " + merchant);
				return getErrorJson(PAYOP_NOT_ENABLED, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
			}
			
			String url = propertyManager.getProperty("gv.service.check.balance.endpoint");
			logger.debug("Hitting " + SERVICE_NAME_CHECK_BALANCE + " API for superPNR : " + superPNR + " with URL: " + url + " and Request JSON : " + requestJson);
			paymentUIHelper.logIntoDB(requestJson, "REQUEST", superPNR, SERVICE_NAME_CHECK_BALANCE);
			
			HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
			String response = httpService.invoke(serviceEndPoint, null, postJsonHeaders(), requestJson, true);
			
			paymentUIHelper.logIntoDB(response, "RESPONSE", superPNR, SERVICE_NAME_CHECK_BALANCE);
			logger.debug(SERVICE_NAME_CHECK_BALANCE + " API response for superPNR : " + superPNR + " is : " + response);
			
			return response;
		} catch(Exception ex) {
			logger.error("Error while CHECK_BALANCE for voucher with superPNR : " + superPNR + ", Exception is : ", ex);
			return getErrorJson(GENERIC_ERROR_MESSAGE, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
		}
	}
	
	public VoucherResponseBean validateVoucher(VoucherRequestBean voucherRequestBean, String merchant, String product) throws Exception {
		
		if(paymentDAO.getIsGVFraudCheckEnabled())
			throw new PayswiftException(FailureCode.GV_FRAUD_CHECK_ENABLED.toString(), FailureCode.GV_FRAUD_CHECK_ENABLED.getCode());
		
		Boolean isGVEnabled = statusProviderService.isPaymentOptionEnabled(merchant, product, PaymentUIUtil.PAYMENT_OPTION_GV);
		if(isGVEnabled == null || !isGVEnabled){
			logger.debug("GV is not enabled for the product : " + product + " and merchant : " + merchant);
			throw new PayswiftException(FailureCode.GV_VALIDATE_PAYOP_NOT_ENABLED.toString(), FailureCode.GV_VALIDATE_PAYOP_NOT_ENABLED.getCode());
		}
		//Validating Voucher is enabled with Prome code or not
		
		String requestJson = gson.toJson(voucherRequestBean);
		String url = propertyManager.getProperty("gv.service.validate.endpoint");
		
		logger.debug("Hitting " + SERVICE_NAME_VALIDATE_VOUCHER + " for superPNR : " + voucherRequestBean.getSuperPnr() + " with URL: " + url + " and Request JSON : " + requestJson);
		paymentUIHelper.logIntoDB(requestJson, "REQUEST", voucherRequestBean.getSuperPnr(), SERVICE_NAME_VALIDATE_VOUCHER);
		
		HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
		String response = httpService.invoke(serviceEndPoint, null, postJsonHeaders(), requestJson, true);
		
		paymentUIHelper.logIntoDB(response, "RESPONSE", voucherRequestBean.getSuperPnr(), SERVICE_NAME_VALIDATE_VOUCHER);
		logger.debug(SERVICE_NAME_VALIDATE_VOUCHER + " API response for superPNR : " + voucherRequestBean.getSuperPnr() + " is : " + response);
		
		return gson.fromJson(response, VoucherResponseBean.class);
	}
	
	public VoucherResponseBean unblockVoucher(VoucherRequestBean voucherRequestBean, String merchant, String product) throws Exception {
		
		Boolean isGVEnabled = statusProviderService.isPaymentOptionEnabled(merchant, product, PaymentUIUtil.PAYMENT_OPTION_GV);
		if(isGVEnabled == null || !isGVEnabled){
			logger.debug("GV is not enabled for the product : " + product + " and merchant : " + merchant);
			throw new PayswiftException(FailureCode.GV_UNBLOCK_PAYOP_NOT_ENABLED.toString(), FailureCode.GV_UNBLOCK_PAYOP_NOT_ENABLED.getCode());
		}
		
		String requestJson = gson.toJson(voucherRequestBean);
		String url = propertyManager.getProperty("gv.service.unblock.endpoint");
		
		logger.debug("Hitting " + SERVICE_NAME_UNBLOCK_VOUCHER + " for superPNR : " + voucherRequestBean.getSuperPnr() + " with URL: " + url + " and Request JSON : " + requestJson);
		paymentUIHelper.logIntoDB(requestJson, "REQUEST", voucherRequestBean.getSuperPnr(), SERVICE_NAME_UNBLOCK_VOUCHER);
		
		HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
		String response = httpService.invoke(serviceEndPoint, null, postJsonHeaders(), requestJson, true);
		
		logger.debug(SERVICE_NAME_UNBLOCK_VOUCHER + " API response for superPNR : " + voucherRequestBean.getSuperPnr() + " is : " + response);
		paymentUIHelper.logIntoDB(response, "RESPONSE", voucherRequestBean.getSuperPnr(), SERVICE_NAME_UNBLOCK_VOUCHER);
		
		return gson.fromJson(response, VoucherResponseBean.class);
	}
	
	public JSONObject redeemVoucher(Map<String, String> portalMap, String merchant, String product) throws Exception {
		
		if(paymentDAO.getIsGVFraudCheckEnabled())
			throw new PayswiftException(FailureCode.GV_FRAUD_CHECK_ENABLED.toString(), FailureCode.GV_FRAUD_CHECK_ENABLED.getCode());
		
		Boolean isGVEnabled = statusProviderService.isPaymentOptionEnabled(merchant, product, PaymentUIUtil.PAYMENT_OPTION_GV);
		if(isGVEnabled == null || !isGVEnabled){
			logger.debug("GV is not enabled for the product : " + product + " and merchant : " + merchant);
			throw new PayswiftException(FailureCode.GV_REDEEM_PAYOP_NOT_ENABLED.toString(), FailureCode.GV_REDEEM_PAYOP_NOT_ENABLED.getCode());
		}

		String url = propertyManager.getProperty("gv.service.redeem.endpoint");
		
		logger.debug("Hitting " + SERVICE_NAME_REDEEM_VOUCHER + " for superPNR : " + portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL) + " with URL: " + url + " and Request Params : " + portalMap.toString());
		paymentUIHelper.logIntoDB(portalMap.toString(), "REQUEST", portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL), SERVICE_NAME_REDEEM_VOUCHER);
		
		HttpEndPoint endPoint = new HttpEndPoint(url, "POST");
		String response = httpService.invoke(endPoint, portalMap);
		
		logger.debug(SERVICE_NAME_REDEEM_VOUCHER + " API response for superPNR : " + portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL) + " is : " + response);
		paymentUIHelper.logIntoDB(response, "RESPONSE", portalMap.get(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL), SERVICE_NAME_REDEEM_VOUCHER);
		
		return new JSONObject(response);
	}
	
	public String getGVProduct(String productCode) {
		String gvProduct = tenantService.getGVProductCode(productCode);
		logger.info("Received GVProductCode for productCode : " + productCode  + " is : " + gvProduct);
		return gvProduct;
	}
	
	public String getGVSource(String merchant) {
		if("travelguru".equalsIgnoreCase(merchant))
			return "TG";
		return "YT";
	}
	
	private JSONObject getErrorJson(String errorMessage, String displayMessage) {
		JSONObject errorJson = new JSONObject();
		try {
			errorJson.put("resCode", 1);
			errorJson.put("resMsg", errorMessage);
			errorJson.put("displayMessage", displayMessage);
			errorJson.put("status", "failure");
		} catch (JSONException ex) {
			logger.error("Error while creating error JSON for check balance API, Exception is : ", ex);
		}
		return errorJson;
	}
	
    private Map<String, String> postJsonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("httpMethod", "POST");
        headers.put("contentType", CONTENT_TYPE_APPLICATION_JSON);
        headers.put("encoding", PaymentUIUtil.UTF_8_ENCODING);
        return headers;
    }

	public String checkVoucher(HttpServletRequest request) throws Exception {
		String voucherNo = request.getParameter(PaymentUIUtil.GV_CODE);
		String superPnr = request.getParameter(PaymentUIUtil.SUPER_PNR);
		JSONObject response = new JSONObject();
		logger.info("Request enters into checkVoucher with super Pnr "+superPnr);
		//Check Promo can allow or not
		if(StringUtils.isEmpty(voucherNo)) {
			logger.error("Request Enters with Blank Vocher Code for super Pnr "+superPnr);
			throw new Exception("Request Enters with Blank Vocher Code");
		}
		
		String paymentDetailsFromDB = paymentDAO.fetchParamForSuperPnrUnzipped(superPnr, "FETCH_ITINERARY_RES");
		
		LobDetailResponse getBookingDetailsResponse = new LobDetailResponse(paymentDetailsFromDB);
		
		//No PromoApplied
		if(YatraUtil.isNullOrEmpty(getBookingDetailsResponse.getPromoCode())) {
			response.put(PaymentUIUtil.RES_CODE,0);
			response.put(PaymentUIUtil.RES_MESSAGE,PaymentUIUtil.SUCCESS);
			response.put(PaymentUIUtil.DIS_MESSAGE,PaymentUIUtil.SUCCESS);
			response.put(PaymentUIUtil.STATUS,PaymentUIUtil.SUCCESS);
			return response.toString();
		} else {
			if(YatraUtil.isNullOrEmpty(getBookingDetailsResponse.getIsGVApplicableWithPromoCode()) || "false".equalsIgnoreCase(getBookingDetailsResponse.getIsGVApplicableWithPromoCode())) {

				response.put(PaymentUIUtil.RES_CODE, 1);
				response.put(PaymentUIUtil.RES_MESSAGE, PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE);
				response.put(PaymentUIUtil.DIS_MESSAGE, PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE);
				response.put(PaymentUIUtil.STATUS, PaymentUIUtil.FAILURE);

				return response.toString();	
			}
		}
		
		
		String validVocherLengthInString = propertyManager.getProperty(PaymentUIUtil.VOCHER_LENGTH);
		int validVocherLength = Integer.valueOf(validVocherLengthInString);
		
		if(voucherNo.length() < validVocherLength) {
			logger.error("Request Enters with Invalid Vocher Code for super Pnr "+superPnr);
			throw new Exception("Request Enters with Invalid Vocher Code");
		}
		JSONObject vouchersConfig = GiftVoucherValidationService.getVouchersConfig();
		if(vouchersConfig.has(voucherNo.substring(0, validVocherLength))
				&& vouchersConfig.getJSONObject(voucherNo.substring(0, validVocherLength)).getBoolean("isPromoSupported")) {
			response.put(PaymentUIUtil.RES_CODE,0);
			response.put(PaymentUIUtil.RES_MESSAGE,PaymentUIUtil.SUCCESS);
			response.put(PaymentUIUtil.DIS_MESSAGE,PaymentUIUtil.SUCCESS);
			response.put(PaymentUIUtil.STATUS,PaymentUIUtil.SUCCESS);
			return response.toString();			
		} else {
			logger.info(PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE);
			JSONObject errorResponse = getErrorJson(PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE, PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE);
			return errorResponse.toString();
		}
	}

	public Map<String, String> validateVoucherWithPromo(VoucherRequestBean requestBean, String promo) {
		Map<String, String> validateMap = new HashMap<String,String>();
		if(YatraUtil.isNullOrEmpty(promo)) {
			logger.info("Promo code is not present so returning status  as  success ");
			validateMap.put(PaymentUIUtil.STATUS,PaymentUIUtil.SUCCESS);
			return validateMap;
		} else {
			String paymentDetailsFromDB = paymentDAO.fetchParamForSuperPnrUnzipped(requestBean.getSuperPnr(), "FETCH_ITINERARY_RES");
			LobDetailResponse getBookingDetailsResponse = new LobDetailResponse(paymentDetailsFromDB);
			if(YatraUtil.isNullOrEmpty(getBookingDetailsResponse.getIsGVApplicableWithPromoCode()) || "false".equalsIgnoreCase(getBookingDetailsResponse.getIsGVApplicableWithPromoCode())) {
				validateMap.put(PaymentUIUtil.STATUS,PaymentUIUtil.RESPONSE_CODE_FAILURE);
				return validateMap;
			}
		}
		
		String validVocherLengthInString = propertyManager.getProperty(PaymentUIUtil.VOCHER_LENGTH);
		int validVocherLength = Integer.valueOf(validVocherLengthInString);
		String validVouchers = propertyManager.getProperty(PaymentUIUtil.VOCHER_NUMBER);
		//Convert into ArrayList
		List<String> validVochersList = Arrays.asList(validVouchers.split(","));
		JSONObject vouchersConfig = GiftVoucherValidationService.getVouchersConfig();
		JSONArray vouchers = requestBean.getVouchers();
		try {
			for(int i = 0; i< vouchers.size();i++) {
				String voucherNo  = vouchers.getJSONObject(i).getString("code");

				if(voucherNo.length() < validVocherLength) {
					logger.error("Promo can not be applied for this voucher Code ");
					validateMap.put(PaymentUIUtil.STATUS, PaymentUIUtil.RESPONSE_CODE_FAILURE);
					validateMap.put(PaymentUIUtil.DIS_MESSAGE, "Promo can not be applied for this voucher Code ");
					return validateMap;
				} else if(!(vouchersConfig.has(voucherNo.substring(0, validVocherLength))
						&& vouchersConfig.getJSONObject(voucherNo.substring(0, validVocherLength)).getBoolean("isPromoSupported"))) {
					logger.error("Promo can not be applied for this voucher Code ");
					validateMap.put(PaymentUIUtil.STATUS, PaymentUIUtil.RESPONSE_CODE_FAILURE);
					validateMap.put(PaymentUIUtil.DIS_MESSAGE, "Promo can not be applied for this voucher Code ");
					return validateMap;
				} /*else if(!validVochersList.contains(voucherNo.substring(0, validVocherLength))) {
					logger.error("Promo can not be applied for this voucher Code ");
					validateMap.put(PaymentUIUtil.STATUS, PaymentUIUtil.RESPONSE_CODE_FAILURE);
					validateMap.put(PaymentUIUtil.DIS_MESSAGE, "Promo can not be applied for this voucher Code ");
					return validateMap;
				}	*/
			}
		} catch(Exception e) {
			logger.error("Exception occurred while validating  whether voucherCode can be used with Promo or not", e);
			validateMap.put(PaymentUIUtil.STATUS, PaymentUIUtil.RESPONSE_CODE_FAILURE);
			validateMap.put(PaymentUIUtil.DIS_MESSAGE, "Promo can not be applied for this voucher Code ");
			return validateMap;
		}
		validateMap.put(PaymentUIUtil.STATUS,PaymentUIUtil.SUCCESS);
		return validateMap;
	}
	
}
