package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import com.yatra.payment.ui.helper.CardsPaymentHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.gv.GiftVoucherService;
import com.yatra.payment.gv.VoucherRequestBean;
import com.yatra.payment.gv.VoucherResponseBean;
import com.yatra.payment.payswift.beans.LobValidationResponse;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.helper.RequestResponseEnhancementHelper;
import com.yatra.payment.ui.service.impl.GiftVoucherValidationService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;

import net.sf.json.JSONArray;

@Service
public class GiftVoucherProcessor implements PayNowProcessor {
	
	private static Logger logger = Logger.getLogger(GiftVoucherProcessor.class);
	
	@Autowired private GiftVoucherService voucherService;
	@Autowired private PayNowProcessor clientAmountVerifier;
	@Autowired private RequestResponseEnhancementHelper rreHelper;
	@Autowired private PaymentUIHelper paymentUIHelper;
	@Autowired private GiftVoucherValidationService voucherValidationService;
	@Autowired private CardsPaymentHelper cardsPaymentHelper;
	
	public static final String FULL_GV_PAYMENT_FAILURE_MSG = "One or more voucher redeemption fail.";
	

	@Override
	public PayNowResponse process(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		try {
			if(!StringUtils.isEmpty(payNowRequest.getVoucherAmount()) && Integer.parseInt(payNowRequest.getVoucherAmount()) > 0) {
				logger.info("In GiftVoucherProcessor with VoucherAmount : " + payNowRequest.getVoucherAmount() + " for superPNR : " + payNowRequest.getSuperPnr());
				VoucherRequestBean requestBean = createVoucherValidateRequest(payNowRequest);
				logger.info("1.debug");
				//Validating Voucher Can Be Applied with or not
				Map<String, String> validateMap = voucherService.validateVoucherWithPromo(requestBean,payNowRequest.getPromoCode());
				logger.info("2.debug");
				String status = validateMap.get(PaymentUIUtil.STATUS);
				logger.info("3.debug");
				if(PaymentUIUtil.RESPONSE_CODE_FAILURE.equalsIgnoreCase(status)) {
					logger.error("Error in GiftVoucherProcessor, ValidateVoucher failed for superPNR : " + payNowRequest.getSuperPnr());
					payNowResponse.setSuccess(false);
					payNowResponse.setFailureCode(FailureCode.GV_VALIDATE_FAILURE);
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.VOUCHER_VALIDATION_FAILURE);
					payNowResponse.setMessage(validateMap.get(PaymentUIUtil.DIS_MESSAGE));
					return payNowResponse;
				} else {
					org.json.JSONArray voucherJsonArray = new org.json.JSONArray(requestBean.getVouchers());
					logger.info("4.debug");
					int statusCode = voucherValidationService.getValidateErrorCode( payNowRequest.getSuperPnr(), voucherJsonArray, requestBean.getLob());
					logger.info("5.debug");
					if(statusCode!=0) {
						logger.info("6.debug");
						logger.error("Error in GiftVoucherProcessor, ValidateVoucher failed for superPNR : " + payNowRequest.getSuperPnr());
						payNowResponse.setSuccess(false);
						payNowResponse.setFailureCode(FailureCode.GV_VALIDATE_FAILURE);
						payNowResponse.setResponseDecision(ResponseDecisionScenarios.VOUCHER_VALIDATION_FAILURE);
						payNowResponse.setMessage(PaymentUIUtil.validateVoucherErrorMsg(statusCode));
						return payNowResponse;
					}
				}
				
				VoucherResponseBean responseBean = voucherService.validateVoucher(requestBean, payNowRequest.getMerchantCode(), payNowRequest.getProductCode());
				logger.info("7.debug");
				if(responseBean == null || responseBean.getResCode() > 0) {
					logger.error("Error in GiftVoucherProcessor, ValidateVoucher failed for superPNR : " + payNowRequest.getSuperPnr());
					payNowResponse.setSuccess(false);
					payNowResponse.setFailureCode(FailureCode.GV_VALIDATE_FAILURE);
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.VOUCHER_VALIDATION_FAILURE);
					payNowResponse.setMessage(responseBean.getDisplayMessage());
					return payNowResponse;
					//throw new PayswiftException(FailureCode.GV_VALIDATE_FAILURE.toString(), FailureCode.GV_VALIDATE_FAILURE.getCode());
				}
				payNowRequest.setVoucherAuthCode(responseBean.getAuthCode());
				payNowRequest.setVoucherValidatedJSON(responseBean.getVouchers().toString());
				payNowRequest.setVoucherRedeemedAmount(String.valueOf(responseBean.getAmount()));
			}
			logger.info("8.debug");
			payNowResponse = clientAmountVerifier.process(payNowRequest, payNowResponse);
			logger.info("9.debug");
			if (payNowResponse.isSuccess() && checkFullVoucherPayment(payNowRequest, payNowResponse.getValidationResponse())) {
				logger.info("10.debug");
				logger.debug("inside Complete Voucher Payment Scenario ");
				Map<String, String> portalMap = rreHelper.preparePortalMap(payNowRequest, payNowResponse);
				org.json.JSONObject response = voucherService.redeemVoucher(portalMap, payNowRequest.getMerchantCode(), payNowRequest.getBookingDetails().getProductCode());
				logger.info("11.debug");
				boolean isSuccess = false;
				if("true".equalsIgnoreCase(response.getString("suc")))
					isSuccess = true;
				if (isSuccess) {
					logger.info("12.debug");
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.COMPLETE_VOUCHER_SUCCESS);
					payNowRequest.setMtxnID(response.getString(PaymentUIUtil.MERCHANT_TXN_ID));
					return payNowResponse;
				} else {
					logger.info("13.debug");
					//Unblocking the voucher
					unblockVoucher(payNowRequest);
					Map<String, String> resMap = new HashMap<String, String>();
					resMap.put("suc", "false");
					resMap.put("superPnr", payNowRequest.getSuperPnr());
					resMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
					paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, resMap, false);
					payNowResponse.setResponseDecision(ResponseDecisionScenarios.COMPLETE_GV_PAYMENT_FAILURE);
					payNowRequest.setMtxnID(response.getString(PaymentUIUtil.MERCHANT_TXN_ID));
					//throw new PayswiftException(FailureCode.GV_REDEEM_FAILURE.getCode(), "GiftVoucher Redemption Failed");
				}
			}
			return payNowResponse;
			
		} catch (PayswiftException ex) {
			logger.error("Exception in GiftVoucherProcessor, ValidateVoucher failed for superPNR : " + payNowRequest.getSuperPnr() + ", Due to : " + ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("Generic Exception in GiftVoucherProcessor, ValidateVoucher failed for superPNR : " + payNowRequest.getSuperPnr() + ", Exception is: " + ex);
			throw new PayswiftException(FailureCode.GV_VALIDATE_EXCEPTION.toString(), FailureCode.GV_VALIDATE_EXCEPTION.setCustomCode(ex.getMessage()).getCode(), ex);
		}
	}
	
	private void unblockVoucher(PayNowRequest payNowRequest) {
		new Thread(new Runnable() {
		    public void run() {
		    	try {
		    		VoucherRequestBean requestBean = createVoucherValidateRequest(payNowRequest);
		    		logger.info("Going to unblock voucher for superPNR : " + payNowRequest.getSuperPnr());
		    		VoucherResponseBean responseBean = voucherService.unblockVoucher(requestBean, payNowRequest.getMerchantCode(), payNowRequest.getProductCode());
		    		logger.info("Unblocking voucher for superPNR : " + payNowRequest.getSuperPnr() + " returned with status : " + responseBean.getStatus());
		    	} catch (Exception ex) {
		    		logger.error("Error while unblocking voucher for superPNR : " + payNowRequest.getSuperPnr() + " and Exception is : " + ex);
		    	}
		    }
		}).start();
	}

	private boolean checkFullVoucherPayment(PayNowRequest payNowRequest, LobValidationResponse lobValidationResponse) {
		logger.debug("Checking Complete Voucher for payop : " + payNowRequest.getPaymentOption());
		WalletInfo walletInfo = payNowRequest.getWalletInfo();
		double walletAmount = 0d;
		double ixigoMoney = 0d;
		if(payNowRequest.getWalletInfo() != null)
			walletAmount = Double.parseDouble(walletInfo.getAmountInRupees());
		if(payNowRequest.getBookingDetails().getIxigoMoney() != null)
			ixigoMoney = Double.parseDouble(payNowRequest.getBookingDetails().getIxigoMoney());
		logger.info("Checking complete voucher payment with walletAmount :" + walletAmount + ", ixigoMoney : " + ixigoMoney + " and voucherAmount : " + payNowRequest.getVoucherRedeemedAmount());
		
		if(!YatraUtil.isNullOrEmpty(payNowRequest.getVoucherRedeemedAmount()) && Double.parseDouble(payNowRequest.getVoucherRedeemedAmount()) > 0) {
			double totalWalletAndIxigoAndVoucherAmount = ixigoMoney + walletAmount + Double.parseDouble(payNowRequest.getVoucherRedeemedAmount());
			if(PaymentUIUtil.PAYMENT_OPTION_GV.equals(payNowRequest.getPaymentOption()) &&
				PaymentUIUtil.validForFullRemeption(String.valueOf(totalWalletAndIxigoAndVoucherAmount), lobValidationResponse.getAmountToCollect())){
				return true;
			}
		}		
		return false;
	}

	private VoucherRequestBean createVoucherValidateRequest(PayNowRequest payNowRequest) {
		try {
			VoucherRequestBean requestBean = new VoucherRequestBean();
			requestBean.setSuperPnr(payNowRequest.getSuperPnr());
			requestBean.setEmailId(payNowRequest.getBookingDetails().getUserEmail());
			requestBean.setSsoToken(payNowRequest.getSSOToken());
			requestBean.setCardHash(cardsPaymentHelper.getHashedCardNumber(payNowRequest));
			requestBean.setAmount(String.valueOf(getVoucherAmount(payNowRequest)));
			requestBean.setLob(voucherService.getGVProduct(payNowRequest.getProductCode()));
			requestBean.setBin(payNowRequest.getCardBin());
			requestBean.setVendorName(payNowRequest.getCardType());
			requestBean.setIsd(payNowRequest.getBookingDetails().getISDCode());
			requestBean.setMobile(payNowRequest.getBookingDetails().getUserMobile());
			requestBean.setSource(voucherService.getGVSource(payNowRequest.getMerchantCode()));
			requestBean.setContext("FULL");
			requestBean.setVouchers(JSONArray.fromObject(payNowRequest.getVoucherJSON()));
			requestBean.setAuthCode(payNowRequest.getVoucherAuthCode());
			requestBean.setPayop(payNowRequest.getPaymentOption());
			requestBean.setVoucherLimitTo(payNowRequest.getBookingDetails().getMaxCountOfVoucherAllowed());
			requestBean.setFlightClass(payNowRequest.getBookingDetails().getFlightClass());
			requestBean.setTripType(payNowRequest.getBookingDetails().getTripType());
			requestBean.setTotPax(payNowRequest.getBookingDetails().getNumTotPax());
			logger.debug(" VoucherRequestBean value : " + requestBean.toString());
			return requestBean;
		} catch (Exception ex) {
			throw new PayswiftException(FailureCode.GV_VALIDATE_EXCEPTION.toString(), FailureCode.GV_VALIDATE_EXCEPTION.setCustomCode("Exception while creating validate voucher request, Exception: " + ex.getMessage()).getCode(), ex);
		}
	}
	
	private int getVoucherAmount(PayNowRequest payNowRequest) {
		try {
			if(payNowRequest.isPGPaymentRequiredForVoucher()) {
				logger.info("Received isPGPaymentRequiredForVoucher value to be true, adding Rs. 1 to the voucherAmount");
				return new Integer(payNowRequest.getVoucherAmount()) + 1;
			}
			
		} catch (Exception ex) {
			logger.error("Error while getting voucher amount for PNR : " + payNowRequest.getSuperPnr() + ", returning orignal amount and Exception is : " + ex);
		}
		return new Integer(payNowRequest.getVoucherAmount());
	}

}
