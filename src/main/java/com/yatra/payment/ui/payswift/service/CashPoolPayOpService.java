package com.yatra.payment.ui.payswift.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.PaymentPageResponse;
import com.yatra.payment.payswift.beans.PaymentRequest;
import com.yatra.payment.ui.display.beans.CashPoolPaymentOption;
import com.yatra.payment.ui.display.beans.PaymentDiv;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.SMEUtil;
import com.yatra.platform.commons.property.PropertyManager;

@Service
public class CashPoolPayOpService {
	private static Logger logger  = Logger.getLogger(CashPoolPayOpService.class);

	@Autowired PropertyManager propertyManager;

	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String STATUS_200_OK = "200";
	public static final String SSO_TOKEN = "ssoToken";
	private static final String HTTP_302 = "302";

	public PaymentDiv updateDiv(PaymentPageResponse pageResponse, PaymentRequest paymentRequest) {
		CashPoolPaymentOption cashPoolPayOp  = null;
		String ssoToken = paymentRequest.getSsoToken();
		List<PaymentOption> updatedPayOps = new ArrayList<>();
		PaymentDiv updatedDiv = new PaymentDiv();
		updatedDiv.setMerchantCode(pageResponse.getPaymentDiv().getMerchantCode());
		updatedDiv.setProductCode(pageResponse.getPaymentDiv().getProductCode());
		updatedDiv.setVersion(pageResponse.getPaymentDiv().getVersion());

		logger.info("Entering into cash pool service for updating payment div ");
		
		for(PaymentOption payOp : pageResponse.getPaymentDiv().getPaymentOptions()) {
			if(PaymentUIUtil.PAYMENT_OPTION_CASHPOOL.equalsIgnoreCase(payOp.getCode())) {
				CashPoolPaymentOption cashPayOpNew = new CashPoolPaymentOption(payOp);
				cashPoolPayOp = cashPayOpNew;
				//updatedPayOps.add(cashPoolPayOp);
			}
			else {
				updatedPayOps.add(payOp);
			}
		}
		updatedDiv.setPaymentOptions(updatedPayOps);


		try {

			if(null != cashPoolPayOp) {
				Map<String,String> userInfoMap = SMEUtil.getSSOTokenDetails(ssoToken);
				
				String userId = userInfoMap.get(PaymentUIUtil.USER_ID);
				String corpId = userInfoMap.get(PaymentUIUtil.CORP_ID);
				//need to give host entry on properties file
				String url = propertyManager.getProperty("cash.pool.get.balance");

				JSONObject requestMap = new JSONObject();
				requestMap.put(PaymentUIUtil.USER_ID, userId);
				requestMap.put(PaymentUIUtil.CORP_ID, corpId);
				requestMap.put(PaymentUIUtil.SSO_TOKEN, ssoToken);

				logger.info("Fetching balance for user id "+ userId + " and corp Id "+corpId +" with url "+ url);
				String response = SMEUtil.sendSimplePostRequest(requestMap.toString(), url);
				logger.info("Response get from cash pool application for user id and corp Id "+userId+" "+corpId+" is "+response);
				if(StringUtils.isEmpty(response)) {
					logger.info("Getting response from Cash Pool Application is Empty for user id "+userId +" corp id "+corpId);
					nullifyCashPool(cashPoolPayOp,updatedDiv); 
					logger.info("Exit from cash pool service for updating payment div");
					return updatedDiv;
				}

				JSONObject responseJson = new JSONObject(response);
				String status = (String) responseJson.get(PaymentUIUtil.STATUS);
				if(!"success".equalsIgnoreCase(status)) {
					logger.info("Getting failure from cahPool service");
					nullifyCashPool(cashPoolPayOp,updatedDiv); 
					logger.info("Exit from cash pool service for updating payment div");
					return updatedDiv;
				}
				Double userBalance = responseJson.getDouble(PaymentUIUtil.BALANCE);
				//Updating cash pool payment option
				String userBalanceString  = Double.toString(userBalance);
				cashPoolPayOp.setUserBalance(userBalanceString);
				updatedPayOps.add(cashPoolPayOp);
				logger.info("Exit from cash pool service for updating payment div");

			}

		} catch (Exception e) {
			logger.error("Exception occured while updating div for exception "+e);
			nullifyCashPool(cashPoolPayOp, updatedDiv);
		}


		return updatedDiv;

	}

	private void nullifyCashPool(CashPoolPaymentOption cashPoolPayOp,PaymentDiv updatedDiv) {
		try {
			logger.info("nullifying cash pool payment option");
			List<PaymentOption> payops = updatedDiv.getPaymentOptions();
			if(cashPoolPayOp != null)
				payops.remove(cashPoolPayOp);
			logger.info("Exiting from nullifying cash pool payment option");
		} catch (Exception e) {
			logger.error("Exception occured while nullifying cash pool payment option");
		}
	}


}
