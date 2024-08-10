package com.yatra.payment.payswift.beans;

import java.util.HashMap;
import java.util.Map;

import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.ui.helper.CardsPaymentHelper;
import com.yatra.payment.ui.payswift.service.NoCostEmiProcessor;
import com.yatra.payment.ui.payswift.service.NoCostEmiService;
import org.apache.commons.lang3.StringUtils;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;
import com.yatra.payment.ui.service.PaytmTokenUtilityService;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import static com.yatra.payment.ui.util.ZipUtils.logger;

public class LobValidateRequest{
	
	private PayNowRequest payNowRequest;
	private PaytmTokenUtilityService paytmTokenUtilityService;
	private NoCostEmiService noCostEmiService;

	private CardsPaymentHelper cardsPaymentHelper;

	public LobValidateRequest(PayNowRequest payNowRequest, PaytmTokenUtilityService paytmTokenUtilityService, NoCostEmiService noCostEmiService,CardsPaymentHelper cardsPaymentHelper) {


		this.payNowRequest = payNowRequest;
		this.paytmTokenUtilityService = paytmTokenUtilityService;
		this.noCostEmiService = noCostEmiService;
		this.cardsPaymentHelper = cardsPaymentHelper;
	}

	public Map<String,String> getRequestMap(){
		Map<String,String> parameterMap = new HashMap<String,String>();
		String payop = payNowRequest.getPaymentOption();
		
		parameterMap.put(PaymentUIUtil.IS_CARD_SAVED_TEMPORARILY, "false");
		parameterMap.put(PaymentUIUtil.CLIENT, payNowRequest.getClient().toString());
		parameterMap.put(PaymentUIUtil.SUPER_PNR, payNowRequest.getSuperPnr());
		parameterMap.put("amountDisplayed", payNowRequest.getAmountDisplayed());
		parameterMap.put("paymentOption", payNowRequest.getPaymentOption());
		String paymentType = payNowRequest.getPaymentType();
		if("paypal".equalsIgnoreCase(paymentType) && !"INR".equalsIgnoreCase(payNowRequest.getCurrency()))
			paymentType = "paypal_cbt";
		parameterMap.put("paymentType", paymentType);
		parameterMap.put("uuid", payNowRequest.getBookingDetails().getUUID());
		parameterMap.put("walletAmountRedeemed",payNowRequest.getWalletAmountRedeemed());
		parameterMap.put("walletId",payNowRequest.getWalletID());
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE, payNowRequest.getPaymentMode());
		parameterMap.put(PaymentUIUtil.PREVIOUS_PAY_OP, payNowRequest.getPreviousPayOp());
		if(PaymentUIUtil.isCardBasedPayop(payop)) {
			parameterMap.put("cardBrand", payNowRequest.getCardBrand());
			parameterMap.put(PaymentUIUtil.CARD_MASK,payNowRequest.getMaskedCardNumber());
			parameterMap.put(PaymentUIUtil.CARD_HASH, cardsPaymentHelper.getHashedCardNumber(payNowRequest));
			if(payNowRequest.getPaymentOption().equalsIgnoreCase("qb"))
			{
				//hardcoded for verify one bin
				//parameterMap.put(PaymentUIUtil.CARD_BIN, "488994");
				if(!PaymentUIUtil.checkIfCorporateProduct(payNowRequest.getProductCode())) {
					String cardId = payNowRequest.getCardId();
					String superPnr = payNowRequest.getSuperPnr();
					String ssoToken = payNowRequest.getSSOToken();
					String cardBrand = payNowRequest.getCardBrand();
					String bin = paytmTokenUtilityService.provideBin(cardId, superPnr, ssoToken, cardBrand);
					parameterMap.put(PaymentUIUtil.CARD_BIN, bin);
				}
				else{
					String cno = PaymentUIUtil.getCardNumberForNonTokenizedQBTxn(payNowRequest.getCardId(),payNowRequest.getSSOToken());
					parameterMap.put(PaymentUIUtil.CARD_BIN,PaymentUIUtil.extractCardBin(cno));
				}

			}
			else{
				parameterMap.put(PaymentUIUtil.CARD_BIN, payNowRequest.getCardBin());

			}

		}
		parameterMap.put(PaymentUIUtil.PRODUCT_CODE, payNowRequest.getProductCode());
		parameterMap.put(PaymentUIUtil.ADDITIONAL_PAYMENT, payNowRequest.getAdditionalPayment());
		parameterMap.put(PaymentUIUtil.YLP, payNowRequest.getYLP());
		if(payNowRequest.getDiscountingStatus()){
			parameterMap.put(PaymentUIUtil.DISCOUNTING_TYPE, payNowRequest.getDiscountingType());
		}
		if ((payop != null 
				&& (payop.equalsIgnoreCase("qb")) || 
				payNowRequest.getBookingDetails().isPassThrough() ||
				"b2bimportpnr".equalsIgnoreCase(payNowRequest.getBookingDetails().getProductCode()))) {
			parameterMap.put(PaymentUIUtil.CARD_ID, payNowRequest.getCardId());
			if(!payop.equalsIgnoreCase("qb")) 
				parameterMap.put(PaymentUIUtil.IS_CARD_SAVED_TEMPORARILY, "true");
			if("b2bimportpnr".equalsIgnoreCase(payNowRequest.getBookingDetails().getProductCode())) 
				parameterMap.put(PaymentUIUtil.AGENT_ID, payNowRequest.getB2BAgentId());
		}
		
		if ((payNowRequest.getPaymentOption() != null 
				&& (payop.equalsIgnoreCase("qb"))) ||
				payNowRequest.getBookingDetails().saveGuestCard() ) {
			parameterMap.put(PaymentUIUtil.CARD_ID, payNowRequest.getCardId());
			parameterMap.put(PaymentUIUtil.UNIQUE_USER_ID, payNowRequest.getUserId());
		}
		if(StringUtils.isNotBlank(payNowRequest.getPromoCode())){
			parameterMap.put(PaymentUIUtil.PROMO_CODE, payNowRequest.getPromoCode());
		}
		if(payNowRequest.getProductCode().equalsIgnoreCase("INT_HOTEL_BKG")
				|| payNowRequest.getProductCode().equalsIgnoreCase("minthoteliosbkg")
				|| payNowRequest.getProductCode().equalsIgnoreCase("minthotelandroidbkg")
				|| payNowRequest.getProductCode().equalsIgnoreCase("smepinthotelandroidbkg")
				|| payNowRequest.getProductCode().equalsIgnoreCase("smeointhotelandroidbkg")
				|| payNowRequest.getProductCode().equalsIgnoreCase("smepinthoteliosbkg")
				|| payNowRequest.getProductCode().equalsIgnoreCase("smeointhoteliosbkg"))
			parameterMap.put(PaymentUIUtil.UNIQUE_USER_ID, payNowRequest.getUserId());
		
		if(("crpdomhotel".equalsIgnoreCase(payNowRequest.getProductCode())
				|| "cdomhotelandroid".equalsIgnoreCase(payNowRequest.getProductCode())
				|| "cdomhotelios".equalsIgnoreCase(payNowRequest.getProductCode())
				|| "cinthotelandroid".equalsIgnoreCase(payNowRequest.getProductCode())
				|| "cinthotelios".equalsIgnoreCase(payNowRequest.getProductCode())
				|| "crpinthotel".equalsIgnoreCase(payNowRequest.getProductCode()))
				&& payNowRequest.getBookingDetails().isBookingDotComFlow())
			parameterMap.put(PaymentUIUtil.UNIQUE_USER_ID, payNowRequest.getUserId());
		
		parameterMap.put(PaymentUIUtil.SSO_TOKEN, payNowRequest.getSSOToken());
		parameterMap.put(PaymentUIUtil.VOUCHER_AMOUNT, payNowRequest.getVoucherAmount());
		parameterMap.put(PaymentUIUtil.APP_VERSION , payNowRequest.getAppVersion());
		if(payop.equalsIgnoreCase("dc") || payop.equalsIgnoreCase("cc") || payop.equalsIgnoreCase("qb")) {
			parameterMap.put(PaymentConstants.IS_CARD_INTERNATIONAL_PARAM, payNowRequest.getPaymentOptionParamMap().getOrDefault("isCardInternational", "false"));
		    }
		
		if(!YatraUtil.isNullOrEmpty(payNowRequest.getVoucherAmount()) && Integer.parseInt(payNowRequest.getVoucherAmount())>0)
			parameterMap.put(PaymentUIUtil.IS_VOUCHER_APPLIED, "true");
		else parameterMap.put(PaymentUIUtil.IS_VOUCHER_APPLIED, "false");
		
		parameterMap.put(PaymentUIUtil.IXIGO_MONEY, payNowRequest.getBookingDetails().getIxigoMoney());
                if ("true".equalsIgnoreCase(payNowRequest.isNoCostEmiPayment())) {
                    parameterMap.put(PaymentUIUtil.IS_NO_COST_EMI_PAYMENT, payNowRequest.getIsNoCostEmiPayment());
                    parameterMap.put(PaymentUIUtil.NO_COST_EMI_CODE, payNowRequest.getNoCostEmiCode());
                    parameterMap.put(PaymentUIUtil.NO_COST_EMI_DISCOUNT, payNowRequest.getNoCostEmiDiscount());
                }else if(payNowRequest.getPaymentOption().equalsIgnoreCase("emi")) {
					try {
						noCostEmiService.fetchNoCostEmiParams(payNowRequest, parameterMap);
					} catch (Exception e) {
						logger.error("Exception occured  while making no cost emi",e);
					}
				}
                return removeParametersIfBlank(parameterMap);
		
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

	public Map<String, String> getRequestMapForApp() {return null;}
}
