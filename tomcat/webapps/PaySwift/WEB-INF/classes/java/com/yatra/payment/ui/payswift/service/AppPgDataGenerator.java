package com.yatra.payment.ui.payswift.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.PaymentClient;
import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.client.beans.WalletRequest;
import com.yatra.payment.payswift.beans.LobDetailRequest;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service
public class AppPgDataGenerator implements PgDataGenerator {

	@Autowired
	PaymentClient paymentClient;
	@Autowired
	LobDetailsService lobDetailsService;
	@Autowired
	LobValidatePaymentService lobValidatePaymentService;

	private static Logger logger = Logger.getLogger(AppPgDataGenerator.class);

	@Override
	public PayNowResponse getPGData(PayNowRequest payNowRequest) throws Exception {

		PayNowResponse payNowResponse = new PayNowResponse();
		payNowResponse.setMessage("Oops,Something went wrong with your Payment.");

		// Call LOB here to get booking Details for fetching maximum YLP .
		// construct Payment Request .
		LobDetailResponse lobBookingDetails = lobDetailsService.fetchItineraryDetailsFromLob(new LobDetailRequest(payNowRequest));

		String walletId = payNowRequest.getWalletID();
		WalletInfo walletInfoResponse = null;
		if (StringUtils.isNotBlank(walletId)) {
			walletInfoResponse = paymentClient.getWalletInfo(payNowRequest.getSuperPnr(), walletId, true, lobBookingDetails.getMaximumRedeemableECash());
			if (!walletInfoResponse.getAmountInRupees().equals(payNowRequest.getWalletAmountRedeemed()) || walletInfoResponse.getStatus().equalsIgnoreCase("false")) {
				logger.info("Wallet Hand Shake Failed , received this from wallet service" + walletInfoResponse.getMessage());
				WalletRequest walletRequest = new WalletRequest();
				walletRequest.setAmountInRupees(walletInfoResponse.getAmountInRupees());
				walletRequest.setMerchantCode(payNowRequest.getMerchantCode());
				walletRequest.setProductCode(payNowRequest.getProductCode());
				walletRequest.setSuperPNR(payNowRequest.getSuperPnr());
				walletRequest.setUserEmail(payNowRequest.getUserEmail());
				walletRequest.setWalletId(walletInfoResponse.getWalletId());
				paymentClient.cancelWalletPayment(walletRequest);
				logger.debug("Sending following response back to App for server LOB validation for TTID " + payNowRequest.getTtid() + "response" + payNowResponse.toString());
				return payNowResponse;
			}
		}

		// LOB Validation
		
		LobValidationResponse lobResponse = lobValidatePaymentService.serverSideValidateWithLobForApp(payNowRequest, payNowRequest.getProductCode());
		if (lobResponse.isEmpty()) {
			logger.debug("Sending following response back to App for server LOB validation for TTID " + payNowRequest.getTtid() + "response" + payNowResponse.toString());
			return payNowResponse;
		}
		
		if(!lobResponse.getSuccess().equalsIgnoreCase("true")){
			payNowResponse.setRurl(lobResponse.getRurl());
			Map<String,String> paramMap = lobResponse.getErrorParamMap();
			payNowResponse.putInPgData(paramMap);
			return payNowResponse;
		}
		
		if(lobResponse.getSuccess().equalsIgnoreCase("true")){
			/*Full Wallet Payment Case*/
			if(walletInfoResponse!=null && 
			   walletInfoResponse.getAmountInRupees().equals(lobResponse.getAmountToCollect()) &&
			   payNowRequest.getPaymentOption().equalsIgnoreCase("ew")){
				
				WalletRequest walletRequest = new WalletRequest();
				walletRequest.setAmountInRupees(walletInfoResponse.getAmountInRupees());
				walletRequest.setMerchantCode(payNowRequest.getMerchantCode());
				walletRequest.setProductCode(payNowRequest.getProductCode());
				walletRequest.setSuperPNR(payNowRequest.getSuperPnr());
				walletRequest.setUserEmail(payNowRequest.getUserEmail());
				walletRequest.setWalletId(walletInfoResponse.getWalletId());
				boolean status = paymentClient.completeWalletPayment(walletRequest);
				
				if(status){
					payNowResponse.setSuccess(true);
					payNowResponse.setRurl(lobResponse.getRurl());
					Map<String,String> resultMap = new HashMap<String,String>();
					resultMap.put("suc", "true");
					resultMap.put("amount", "0");
					resultMap.put("superPnr", payNowRequest.getSuperPnr());
					resultMap.put("wallet_amount",walletInfoResponse.getAmountInRupees());
					resultMap.put("wallet_id", walletInfoResponse.getWalletId());
					payNowResponse.putInPgData(resultMap);
					payNowResponse.setMessage(null);
					logger.debug("Sending following response back to paySwift UI for server LOB validation for TTID "+payNowRequest.getTtid()+
							"response"+payNowResponse.toString());
					return payNowResponse;
				}
				else{
					logger.debug("Sending following response back to paySwift UI for server LOB validation for TTID "+payNowRequest.getTtid()+
							"response"+payNowResponse.toString());
					return payNowResponse;
				}
			}
			else{
				String walletAmount = null;
				payNowResponse.setRurl("");
				payNowResponse.setSuccess(true);
				Map<String,String> pgDataMap = payNowRequest.getParameterMap();
				pgDataMap.put(PaymentUIUtil.RURL, lobResponse.getRurl());
				if(walletInfoResponse!=null){
					walletAmount = walletInfoResponse.getAmountInRupees();
					pgDataMap.put("wallet_amount", walletAmount);
				}
				pgDataMap.put(PaymentUIUtil.AMOUNT,getAmountToCollectFromPG(lobResponse.getAmountToCollect(), walletAmount));
				
				pgDataMap.put(PaymentUIUtil.MOBILE,payNowRequest.getUserMobile());
				pgDataMap.put(PaymentUIUtil.EMAIL, payNowRequest.getUserEmail());
				pgDataMap.put(PaymentUIUtil.ORIGIN_COUNTRY, payNowRequest.getOrigin());
				pgDataMap.put(PaymentUIUtil.DESTINATION_COUNTRY, payNowRequest.getDestination());
				pgDataMap.put("product_code", payNowRequest.getProductCode());
				pgDataMap.put(PaymentUIUtil.MODE, PaymentUIUtil.MODE_PURCHASE);
				pgDataMap.put(PaymentUIUtil.HASH_KEY, paymentClient.generateHashKey(PaymentUIUtil.createStringDataFromMapWithSeparator(payNowRequest.getParameterMap(),"|"), payNowRequest.getTtid(), getAmountToCollectFromPG(lobResponse.getAmountToCollect(), walletAmount), payNowRequest.getCVVRequired(), lobResponse.getIsZeroAmountTxn(), payNowRequest.getBookingDetails().getIxigoMoney(),payNowRequest));
				pgDataMap.put("tdate", payNowRequest.getTravelDate());
				payNowResponse.putInPgData(pgDataMap);// need to populate this with extra parameters .
				payNowResponse.setMessage(null);
				return payNowResponse;
			}
		}
		

		return payNowResponse;
	}
	private String getAmountToCollectFromPG(String totalAmount , String walletAmount){
		if(StringUtils.isBlank(walletAmount)){
			return totalAmount;
		}
		return new BigDecimal(totalAmount).subtract(new BigDecimal(walletAmount)).toPlainString();
	}
}
