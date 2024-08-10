package com.yatra.payment.gv;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.beans.LobDetailRequest;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.enums.ClientType;
import com.yatra.payment.ui.service.impl.GiftVoucherValidationService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

@Controller
@RequestMapping("/gift-voucher/*")
public class GiftVoucherController {

	private static Logger logger = Logger.getLogger(GiftVoucherController.class);
	private static final String GENERIC_ERROR_MESSAGE = "System error occoured.";
	private static final String GENERIC_ERROR_DISPLAY_MESSAGE = "check balance failed, Please try again.";

	@Autowired
	private GiftVoucherService voucherService;
	@Autowired GiftVoucherValidationService voucherValidationService;
	@Autowired PayswiftStagesDAOImpl paymentDAO;
	
	@RequestMapping(value = "/type", method = RequestMethod.GET)
	public @ResponseBody String getType(HttpServletRequest request, HttpServletResponse response) {

		return voucherService.getType(
				request.getParameter(PaymentUIUtil.GV_CODE),
				request.getParameter(PaymentUIUtil.MERCHANT_CODE),
				request.getParameter(PaymentUIUtil.PRODUCT_CODE),
				request.getParameter(PaymentUIUtil.SUPER_PNR)
				);
	}

	@RequestMapping(value = "/{merchant}/{product}/{superPnr}/check-balance", method = RequestMethod.POST)
	public @ResponseBody String checkBalance(
			@RequestBody String requestJson, 
			@PathVariable(PaymentUIUtil.MERCHANT_CODE) String merchant,
			@PathVariable(PaymentUIUtil.PRODUCT_CODE) String product, 
			@PathVariable(PaymentUIUtil.SUPER_PNR) String superPnr, 
			HttpServletRequest request,	
			HttpServletResponse response) {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		try {
			JSONObject jsonObject = new JSONObject(requestJson);
			if(ssoToken != null &&  StringUtils.isNotEmpty(ssoToken) ) {
				jsonObject.put(PaymentUIUtil.SSO_TOKEN, ssoToken);
			}
			
			String paymentDetailsFromDB = paymentDAO.fetchParamForSuperPnrUnzipped(superPnr, "FETCH_ITINERARY_RES");
			LobDetailResponse lobDetails = new LobDetailResponse(paymentDetailsFromDB);
			int maxcount = lobDetails.getMaxCountOfVoucherAllowed();
			if(maxcount > 0)
				jsonObject.put(PaymentUIUtil.VOUCHER_LIMIT_TO, maxcount);
			jsonObject.put(PaymentUIUtil.FLIGHT_CLASS,lobDetails.getFlightClass());
			jsonObject.put(PaymentUIUtil.TRIP_TYPE, lobDetails.getTripType());
			jsonObject.put(PaymentUIUtil.TOTAL_PAX,lobDetails.getNumTotPax());
			
			int validateErrorCode = voucherValidationService.getValidateErrorCode(superPnr,jsonObject.getJSONArray("vouchers"), jsonObject.optString("lob"));
			if(validateErrorCode == 0)
				return voucherService.checkBalance(jsonObject.toString(), merchant, product, superPnr);
			else return PaymentUIUtil.validateVoucherErrorJson(validateErrorCode).toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occur while getting vocher balance for super Pnr "+superPnr+" "+e);
			return getErrorJson(GENERIC_ERROR_MESSAGE, GENERIC_ERROR_DISPLAY_MESSAGE).toString();
		}
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
	
	@RequestMapping(value = "checkVocherType")
	@ResponseBody
	public String checkVocherType(HttpServletRequest request) {
		logger.info("Request enters into check vocher type controller ");
		try {
			return voucherService.checkVoucher(request);
		} catch (Exception e) {
			logger.info("Exception occured while checking voucher Typer "+e);
			JSONObject errorResponse = getErrorJson(PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE, PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE);
			return errorResponse.toString();
		}
	}




	@ResponseBody
	@RequestMapping(value = "/validateAndGetType", method = RequestMethod.GET)
	public String validateAndGetType(HttpServletRequest request, HttpServletResponse response){
		String validatedResponse = "";

		String voucherType = voucherService.getType(
				request.getParameter(PaymentUIUtil.GV_CODE),
				request.getParameter(PaymentUIUtil.MERCHANT_CODE),
				request.getParameter(PaymentUIUtil.PRODUCT_CODE),
				request.getParameter(PaymentUIUtil.SUPER_PNR)
		);

		try{
			JSONObject voucherTypeObject = new JSONObject(voucherType);
			String type = voucherTypeObject.getString("type");
			if (!type.equalsIgnoreCase("QC")) {
				return voucherTypeObject.toString();
			}
			try {
				validatedResponse = voucherService.checkVoucher(request);
			} catch (Exception e) {
				logger.error("Exception occured while validating voucher with super_pnr ", e );
			}
			JSONObject validatedResponseObject = new JSONObject(validatedResponse);
			String status = validatedResponseObject.getString("status");
			if (status.equalsIgnoreCase("Success")) {
				JSONObject validateAndGetTypeObj = new JSONObject();
				String displayMessage = validatedResponseObject.getString("displayMessage");
				String resCode = validatedResponseObject.getString("resCode");
				String resMsg = validatedResponseObject.getString("resMsg");
				String isPaymentRequired = voucherTypeObject.getString("isPaymentRequired");

				validateAndGetTypeObj.put("displayMessage", displayMessage);
				validateAndGetTypeObj.put("resCode", resCode);
				validateAndGetTypeObj.put("resMsg", resMsg);
				validateAndGetTypeObj.put("isPaymentRequired", isPaymentRequired);
				validateAndGetTypeObj.put("type", type);
				validateAndGetTypeObj.put("status", status);
				return validateAndGetTypeObj.toString();
			} else {
				JSONObject errorResponse = getErrorJson(PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE, PaymentUIUtil.PROMO_VOUCHER_FAILURE_MESSAGE);
				return errorResponse.toString();
			}
		}catch(Exception e){
			logger.error("Exception occured while validating voucher with super_pnr ", e );
			return voucherType;
		}
	}

}

