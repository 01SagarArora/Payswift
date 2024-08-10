package com.yatra.payment.payswift.beans;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.yatra.payment.client.beans.WalletInfo;
import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.ui.enums.ClientType;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.SMEUtil;
import com.yatra.platform.request.MapBasedRequest;
import com.yatra.platform.util.YatraUtil;

import static com.yatra.payment.ui.util.PaymentUIUtil.SSO_TOKEN;

public class PayNowRequest extends MapBasedRequest {

	private static final long serialVersionUID = -2630399568993809105L;

	private LobDetailResponse bookingDetails;
	private WalletInfo walletInfo;
	private String host;
	private HttpServletRequest httpServletRequest;
	private Map<String,String> paymentOptionParamMap ;
	private String userId ;
	private String cashPoolUserId;
	private String cashPoolCorpId;

	private static Logger logger = Logger.getLogger(PayNowRequest.class);

	public PayNowRequest(Map<String, String> parameterMap) {
		super(parameterMap);
		createPaymentOptionParameterMap();

	}

	public PayNowRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		createPaymentOptionParameterMap();
		try {
			this.httpServletRequest = httpRequest;
			URL url =  new URL(httpRequest.getRequestURL().toString());
			this.host = "https://"+url.getHost();
			logger.info("url host is:"+host);
			setSsoTokenToRequestParameters(httpRequest);
		} catch (MalformedURLException e) {
			logger.warn("Error in getting host from request url"+e.getMessage());
		}
		setUserIdFromCookie(httpRequest);	
		setCashPoolUserDetailsIfPresent(PaymentUIUtil.getSSOToken(httpRequest));
	}
	private void setCashPoolUserDetailsIfPresent(String ssoToken) {
		Map<String,String> userDetailsMap = SMEUtil.getSSOTokenDetails(ssoToken);
		setCashPoolCorpId(userDetailsMap.getOrDefault(PaymentUIUtil.CORP_ID,null));
		setCashPoolUserId(userDetailsMap.getOrDefault(PaymentUIUtil.USER_ID,null));
	}

	private void setUserIdFromCookie(HttpServletRequest httpRequest) {

		Cookie userIdCookie = PaymentUIUtil.getPerticularCookie(httpRequest, PaymentUIUtil.UNIQUE_USER_ID);
		if(userIdCookie != null){
			setUserId(userIdCookie.getValue());
		}
	}

	private void createPaymentOptionParameterMap() {
		paymentOptionParamMap = new HashMap<String,String>();
		String paymentOptionParameters = getPaymentOptionParameters();
		if(StringUtils.isEmpty(paymentOptionParameters) || "null".equalsIgnoreCase(paymentOptionParameters)){
			return;
		}
		String [] parameterArray = paymentOptionParameters.split("\\|");
		for(String param : parameterArray){
			String paramPair[] = param.split("=");
			String paramName = paramPair[0];
			String paramValue = paramPair[1];
			paymentOptionParamMap.put(paramName,paramValue);
		}
	}

	public void setPaymentOptionParamMap(Map<String, String> paymentOptionParamMap) {
		this.paymentOptionParamMap = paymentOptionParamMap;
	}
	
	public String getAppVersion() {
		return getParameter(PaymentUIUtil.APP_VERSION);
	}

	public String getSuperPnr() {
		return getParameter(PaymentUIUtil.SUPER_PNR);
	}

	public String getPaymentProcessType() {
		return getParameter(PaymentUIUtil.PAYMENT_PROCESS_TYPE);
	}

	public String getCVVRequired() {
		String cvvRequired = getParameter(PaymentUIUtil.CVV_REQUIRED);
		if(YatraUtil.isNullOrEmpty(cvvRequired))
			cvvRequired = "true";
		return cvvRequired;
	}

	public String getPaymentType(){
		String paymentOption= getPaymentOption();
		if(paymentOption.equals("nb") || paymentOption.equals("mw") || 
				paymentOption.equals("atm") || paymentOption.equals("emi") ||
				paymentOption.equals("rewards") || PaymentConstants.PAY_OP_PAY_LATER.equals(paymentOption)) {
			return getBankCode();
		} else if (paymentOption.equals("cc")||paymentOption.equals("dc")){
			return getCardType();
		} else if (paymentOption.equals("qb")||paymentOption.equals("corpCard")){
			return getParameter("cardType");
		} else if(paymentOption.equalsIgnoreCase("paypal"))
			return paymentOption;
		else if(paymentOption.equalsIgnoreCase("upi"))
			return "upi";
		else if(paymentOption.equalsIgnoreCase("tez") || paymentOption.equalsIgnoreCase("tez_pwa"))
			return "tez";
		else if(paymentOption.equalsIgnoreCase("phonePe"))
			return "phonePe";
		else if(paymentOption.equalsIgnoreCase("twid"))
			return "twid";

		return "";
	}

	public String getCardBrand(){
		String cardBrand = "";
		String paymentOption= getPaymentOption();
		if (paymentOption.equals("cc")||paymentOption.equals("dc")){
			cardBrand =  getCardType();
		}
		else if (paymentOption.equals("qb")||paymentOption.equals("corpCard")){
			cardBrand =  getParameter("cardBrand");
		}
		return cardBrand;
	}
	public String getTtid() {
		return getParameter(PaymentUIUtil.TTID);
	}

	public String getDiscountingType() {
		return getBookingDetails().getDiscountType();
	}

	public String getPreviousPayOp() {
		return getParameter(PaymentUIUtil.PREVIOUS_PAY_OP);
	}

	public String getCardType() {
		return getParameter(PaymentUIUtil.CARD_TYPE);
	}

	public String getBankCode() {
		return getParameter(PaymentUIUtil.BANK_CODE);
	}
	public String getAmountDisplayed() {
		return getParameter(PaymentUIUtil.AMOUNT_DISPLAYED);
	}
	public String getAmount() {
		return getParameter(PaymentUIUtil.PARTIAL_AMOUNT);
	}
	public String getPaymentOption() {
		return getParameter(PaymentUIUtil.PAYMENT_OPTION);
	}
	public String getSubPaymentOption() {
		return getParameter(PaymentUIUtil.SUB_PAYMENT_OPTION);
	}
	public String getWalletAmountRedeemed() {
		return getParameter(PaymentUIUtil.WALLET_AMOUNT);
	}
	public String getPaymentMode() {
		return getParameter(PaymentUIUtil.PAYMENT_MODE);
	}
	public String getProductCode() {
		return getParameter(PaymentUIUtil.PRODUCT_CODE);
	}
	public String getMerchantCode() {
		return getParameter(PaymentUIUtil.MERCHANT_CODE);
	}
	public String getPaymentOptionParameters(){
		return getParameter(PaymentUIUtil.PAYMENT_OPTION_PARAMETERS);
	}
	public String getYLP(){
		return getParameter(PaymentUIUtil.YLP);
	}
	public String getBookingDetailAPIEndpoint() {
		return getParameter(PaymentUIUtil.DETAIL_API_ENDPOINT);
	}
	public void setPaymentOptionParameters(String paymentOptionParams){
		Map<String,String> paramMap = getParameterMap();
		if(paramMap !=null){
			getParameterMap().put(PaymentUIUtil.PAYMENT_OPTION_PARAMETERS, paymentOptionParams);
		}
	}

	public String getWalletID(){
		return getParameter(PaymentUIUtil.WALLET_ID);
	}

	public String getUserEmail() {
		return getParameter(PaymentUIUtil.EMAIL);
	}

	public String getUserMobile() {
		return getParameter(PaymentUIUtil.MOBILE);
	}
	public String getOrigin() {
		return getParameter(PaymentUIUtil.ORIGIN_COUNTRY);
	}
	public String getDestination() {
		return getParameter(PaymentUIUtil.DESTINATION_COUNTRY);
	}
	public String getTravelDate() {
		return getParameter("tdate");
	}
	public String getCurrency() {
		return getParameter(PaymentUIUtil.CURRENCY);
	}
	public void putPgDataMap(Map<String, String> pgDataMap) {
		this.getParameterMap().putAll(pgDataMap);
	}
	@Override
	public String toString() {

		Map<String,String> parameters =  getParameterMap();

		StringBuffer buff = new StringBuffer();
		Set<String> keys = parameters.keySet();
		for (String key : keys) {
			try {
				if(key.equalsIgnoreCase("paymentOptionParameters")){
					maskSensitiveInformation(buff,getParameter(key));
				}

				buff.append(key)
				.append("=")
				.append((key.equalsIgnoreCase("paymentOptionParameters")||key.equalsIgnoreCase("cno")||key.equalsIgnoreCase("ccsc"))
						&& getParameter(key) != null ? null : getParameter(key));
				buff.append(",");
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace(System.out);
			}

		}
		return buff.toString();

	}
	private void maskSensitiveInformation(StringBuffer buff, String paymentOptionParameters) {
		paymentOptionParameters = paymentOptionParameters.replaceAll("\\|\\|", "|");
		String [] parameterArray = paymentOptionParameters.split("\\|");
		for(String param : parameterArray){
			String paramPair[] = param.split("=");
			String paramName = paramPair[0];
			String paramValue = paramPair[1];

			if(paramName.equalsIgnoreCase("cno")||paramName.equalsIgnoreCase("cardNumber")) {
				paramValue = PaymentUIUtil.maskCreditCardNumber(paramValue);
			} else if(paramName.equalsIgnoreCase("ccsc") ||paramName.equalsIgnoreCase("cexpm") ||paramName.equalsIgnoreCase("cexpy")){
				if(paramValue != null) {
					StringBuilder stringBuilder = new StringBuilder();
					for(int index = 0; index < paramValue.length(); index++) {
						if(!Character.isWhitespace(paramValue.charAt(index))) {
							stringBuilder.append("x");
						} else {
							stringBuilder.append(" ");
						}
					}
					paramValue = stringBuilder.toString();
				}
			}
			buff.append(paramName);
			buff.append("=");
			buff.append(paramValue);
			buff.append(",");

		}
	}
	public LobDetailResponse getBookingDetails() {
		return bookingDetails;
	}

	public String getMaskedCardNumber(){

		String cardNumber = this.paymentOptionParamMap.get("cno");
		if(StringUtils.isBlank(cardNumber)){
			this.paymentOptionParamMap.get("cardNumber");
		}
		return PaymentUIUtil.maskCreditCardNumber(cardNumber);
	}

	public String getHashedCardNumber(){
		String cardNumber = "";
		String product = this.bookingDetails.getProductCode();
		boolean isCorporateProduct = PaymentUIUtil.checkIfCorporateProduct(product);
		//TODO add handling of express card service as well
		if("qb".equalsIgnoreCase(getParameter(PaymentUIUtil.PAYMENT_OPTION)) && !isCorporateProduct)
			return PaymentUIUtil.getCardNumberForQBTxn(this.getCardId(), this.getSSOToken());
		else {
			cardNumber = this.paymentOptionParamMap.get("cno");
			if(StringUtils.isBlank(cardNumber)){
				this.paymentOptionParamMap.get("cardNumber");
			}
		}

		return PaymentUIUtil.getHashedValue(cardNumber,"SHA-512");
	}

	public void setBookingDetails(LobDetailResponse bookingDetails) {
		this.bookingDetails = bookingDetails;
	}
	public WalletInfo getWalletInfo() {
		return walletInfo;
	}
	public void setWalletInfo(WalletInfo walletInfo) {
		this.walletInfo = walletInfo;
	}

	public String getPromoCode(){
		return getParameter(PaymentUIUtil.PROMO_CODE);
	}

	public ClientType getClient() {
		try {
			if (getParameterMap().containsKey(PaymentUIUtil.CLIENT)) {
				return ClientType.valueOf(getParameter(PaymentUIUtil.CLIENT));
			}
		} catch (Exception e) {
		}
		return ClientType.DESKTOP;
	}

	public boolean getDiscountingStatus() {
		return getVisaDiscountingStatus()||getOtherDiscountingStatus();
	}

	private boolean getVisaDiscountingStatus() {
		if (getCardDiscountParameter() != null && 
				getCardDiscountParameter().equalsIgnoreCase("true") && 
				bookingDetails != null && 
				(getDiscountingType().equalsIgnoreCase("VISA") || getDiscountingType().equalsIgnoreCase("CARD"))) {
			return true;
		}
		return false;
	}

	private boolean getOtherDiscountingStatus() {
		if (getOtherDiscountParameter() != null && 
				getOtherDiscountParameter().equalsIgnoreCase("true") && 
				bookingDetails != null && 
				(!getDiscountingType().equalsIgnoreCase("VISA") && !getDiscountingType().equalsIgnoreCase("CARD"))) {
			return true;
		}
		return false;
	}

	public String getCardBin() {

		/*	String paymentOptionParameters = getPaymentOptionParameters();
		String [] parameterArray = paymentOptionParameters.split("\\|");
		for(String param : parameterArray){
			String paramPair[] = param.split("=");
			String paramName = paramPair[0];
			String paramValue = paramPair[1];
			if(paramName.equalsIgnoreCase("cno")||paramName.equalsIgnoreCase("cardNumber")){
				return PaymentUIUtil.extractBin(paramValue);
			}else{
			}
		}
		return null;*/
		String cardNumber = this.paymentOptionParamMap.get("cno");
		if(StringUtils.isBlank(cardNumber)){
			this.paymentOptionParamMap.get("cardNumber");
		}
		return PaymentUIUtil.extractBin(cardNumber);

	}

	public void setCardId(String cardId){
		this.paymentOptionParamMap.put(PaymentUIUtil.CARD_ID, cardId);
	}

	public String getCardId() {

		/*
		 * String paymentOptionParameters = getPaymentOptionParameters(); String
		 * [] parameterArray = paymentOptionParameters.split("\\|"); for(String
		 * param : parameterArray){ String paramPair[] = param.split("=");
		 * String paramName = paramPair[0]; String paramValue = paramPair[1];
		 * if(
		 * paramName.equalsIgnoreCase("cardid")||paramName.equalsIgnoreCase("cardId"
		 * )){ return paramValue; }else{ } } return null;
		 */
		String cardNumber = this.paymentOptionParamMap.get("cardId");
		if (StringUtils.isBlank(cardNumber)) {
			cardNumber = this.paymentOptionParamMap.get("cardid");
		}
		return cardNumber;

	}
	
	public void setB2BAgentId(String agentId){
		this.paymentOptionParamMap.put(PaymentUIUtil.B2B_AGENT_ID, agentId);
	}
	
	public String getB2BAgentId(){
		String agentId = this.paymentOptionParamMap.get(PaymentUIUtil.B2B_AGENT_ID);
		return agentId;
	}
	

	public String getCardDiscountParameter() {
		return getParameter(PaymentUIUtil.CARD_DISCOUNTING_STATUS);
	}

	public String getOtherDiscountParameter() {
		return getParameter(PaymentUIUtil.OTHER_DISCOUNTING_STATUS);
	}

	public String getHost() {
		if(StringUtils.isNotBlank(host))
			return host;
		return YatraUtil.getPropertyManager().getProperty("default.payment.portal.host");
	}

	public String getUUID() {
		return getParameter(PaymentUIUtil.UUID);
	}

	public String getSSOToken() {
		String ssoToken = PaymentUIUtil.getSSOToken(this.httpServletRequest);
		if(ssoToken == null) {
			return getParameter(SSO_TOKEN);
		}
		return ssoToken;
	}

	public Map<String, String> getPaymentOptionParamMap() {
		return this.paymentOptionParamMap;
	}



	public void setPayOpBasedOnCardBinType(String payop){
		String payOpParams = getPaymentOptionParameters();
		if(StringUtils.isBlank(payOpParams)){
			setPaymentOptionParameters(PaymentConstants.PAYMENT_OPTION_PARAM + "=" + payop);
		}
		else {
			if (payOpParams.endsWith("|")) {
				setPaymentOptionParameters(payOpParams+PaymentConstants.PAYMENT_OPTION_PARAM+"="+payop);
			}
			else {
				setPaymentOptionParameters(payOpParams+"|payop="+payop);
			}
		}

	}

	public void setWalletID(String walletId){
		Map<String,String> paramMap = getParameterMap();
		if(paramMap !=null){
			getParameterMap().put(PaymentUIUtil.WALLET_ID, walletId);
		}
		String payOpParams = getPaymentOptionParameters();
		if(StringUtils.isBlank(payOpParams)){
			setPaymentOptionParameters("wallet_id="+walletId);
		}
		else{
			if(payOpParams.endsWith("|")){
				setPaymentOptionParameters(payOpParams+"wallet_id="+walletId);
			}
			else
			{
				setPaymentOptionParameters(payOpParams+"|wallet_id="+walletId);
			}
		}

		/*public String getAuthWalletID(){
		return wallet_id;
	}*/


	}

	public String getPackageName() {
		return getParameter(PaymentUIUtil.PACKAGE);
	}

	public String getSdkVersion() {
		return getParameter(PaymentUIUtil.SDK_VERSION);
	}




	public void setWalletAmount(String walletAmount){
		Map<String,String> paramMap = getParameterMap();
		if(paramMap !=null){
			getParameterMap().put(PaymentUIUtil.WALLET_AMOUNT, walletAmount);
		}
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAppCybersourceFingerprintId() {
		return getParameter(PaymentUIUtil.APP_CYBERSOURCE_FINGERPRINT_ID);
	}

	public String getUserId() {
		return userId;
	}


	public String getAmountToRedeem() {
		return getParameter(PaymentUIUtil.AMOUNT_TO_REDEEM);
	}

	public void setAmountToRedeem(String amount) {
		Map<String,String> paramMap = getParameterMap();
		if(paramMap !=null){
			getParameterMap().put(PaymentUIUtil.AMOUNT_TO_REDEEM, amount);
		}
	}

	public String getAdditionalPayment() {
		return getParameter(PaymentUIUtil.ADDITIONAL_PAYMENT);
	}



	public String getVoucherAmount() {
		return getParameter(PaymentUIUtil.VOUCHER_AMOUNT);
	}

	public String getVoucherJSON() {
		return getParameter(PaymentUIUtil.VOUCHER_JSON);
	}

	public void setVoucherAuthCode(String authCode) {
		Map<String, String> paramMap = getParameterMap();
		if (paramMap != null) {
			getParameterMap().put(PaymentUIUtil.VOUCHER_AUTH_CODE, authCode);
		}
	}

	public String getVoucherAuthCode() {
		return getParameter(PaymentUIUtil.VOUCHER_AUTH_CODE);
	}

	public void setVoucherValidatedJSON(String json) {
		Map<String, String> paramMap = getParameterMap();
		if (paramMap != null) {
			getParameterMap().put(PaymentUIUtil.VOUCHER_VALIDATED_JSON, json);
		}
	}

	public String getVoucherValidatedJSON() {
		return getParameter(PaymentUIUtil.VOUCHER_VALIDATED_JSON);
	}

	public void setVoucherRedeemedAmount(String amount) {
		Map<String, String> paramMap = getParameterMap();
		if (paramMap != null) {
			getParameterMap().put(PaymentUIUtil.VOUCHER_REDEEMED_AMOUNT, amount);
		}
	}

	public String getVoucherRedeemedAmount() {
		return getParameter(PaymentUIUtil.VOUCHER_REDEEMED_AMOUNT);
	}

	public String getPGPaymentRequiredForVoucherParameter() {
		return getParameter(PaymentUIUtil.GV_IS_PG_PAYMENT_REQUIRED);
	}

	public boolean isPGPaymentRequiredForVoucher() {
		if (getPGPaymentRequiredForVoucherParameter() != null && getPGPaymentRequiredForVoucherParameter().equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}


	public void setMtxnID(String mtxnid) {
		Map<String, String> paramMap = getParameterMap();
		if (paramMap != null) {
			getParameterMap().put(PaymentUIUtil.MERCHANT_TXN_ID, mtxnid);
		}
	}

	public String getMtxnID() {
		return getParameter(PaymentUIUtil.MERCHANT_TXN_ID);
	}

	public boolean isAmazonPaySDKFlow(){
		String amazonSdkFlag = getParameter(PaymentUIUtil.AMAZON_PAY_SDK_FLOW);
		String payop = getPaymentOption();
		String bankCode = getBankCode();
		if("false".equals(amazonSdkFlag))
			return false;
		else if("true".equals(amazonSdkFlag)) {
			if(payop.equalsIgnoreCase("amazonPay")	
					|| (payop.equalsIgnoreCase("mw") && "AMPY".equalsIgnoreCase(bankCode)))
				return true;
		}
		return false;

	}

	public boolean isNonSDKFlow(){
		return "true".equals(getParameter(PaymentUIUtil.IS_NON_SDK_FLOW));
	}

	public boolean isGoogleTezFlow(){
		return "true".equals(getParameter(PaymentUIUtil.GOOGLE_TEZ_FLOW));
	}
	public boolean isGoogleTezAndroidFlow() {
		return "true".equals(getParameter(PaymentUIUtil.GOOGLETEZ_ANDROID_FLOW));	}

	public String isNoCostEmiPayment() {
		if("true".equalsIgnoreCase(getParameter(PaymentUIUtil.NCEMI_FLAG)))
			return "true";
		else return "false";
	}
	public void setIsNoCostEmiPayment(String flag) {
		getParameterMap().put(PaymentUIUtil.NCEMI_FLAG,flag);
	}

	public String getIsNoCostEmiPayment() {
		return getParameter(PaymentUIUtil.IS_NO_COST_EMI_PAYMENT);
	}

	public String getNoCostEmiDiscount() {
		return getParameter(PaymentUIUtil.NO_COST_EMI_DISCOUNT);
	}

	public String getNoCostEmiCode() {
		return getParameter(PaymentUIUtil.NO_COST_EMI_CODE);
	}
	public String getEMIBankCode() {
		return getParameter(PaymentUIUtil.EMI_BANK);
	}
	public String getEMITenure() {
		return getParameter(PaymentUIUtil.EMI_TENURE);
	}

	public String getNoCostEmiActualAmount() {
		return getParameter(PaymentUIUtil.ACTUAL_AMOUNT);
	}

	public void setNoCostEmiParams(String noCostEmiCode, String noCostEmiDiscount, String actualAmount, String bank, String tenure) {
		getParameterMap().put(PaymentUIUtil.IS_NO_COST_EMI_PAYMENT, String.valueOf(Boolean.TRUE));
		getParameterMap().put(PaymentUIUtil.NO_COST_EMI_CODE, noCostEmiCode);
		getParameterMap().put(PaymentUIUtil.NO_COST_EMI_DISCOUNT, noCostEmiDiscount);
		getParameterMap().put(PaymentUIUtil.ACTUAL_AMOUNT, actualAmount);
		getParameterMap().put(PaymentUIUtil.EMI_BANK, bank);
		getParameterMap().put(PaymentUIUtil.EMI_TENURE, tenure);

	}
	
	public void setNoCostEmiParams(String noCostEmiCode, String noCostEmiDiscount, String actualAmount, String bank, String tenure, Map<String, String> parameterMap) {
		parameterMap.put(PaymentUIUtil.IS_NO_COST_EMI_PAYMENT, String.valueOf(Boolean.TRUE));
		parameterMap.put(PaymentUIUtil.NO_COST_EMI_CODE, noCostEmiCode);
		parameterMap.put(PaymentUIUtil.NO_COST_EMI_DISCOUNT, noCostEmiDiscount);
		parameterMap.put(PaymentUIUtil.ACTUAL_AMOUNT, actualAmount);
		parameterMap.put(PaymentUIUtil.EMI_BANK, bank);
		parameterMap.put(PaymentUIUtil.EMI_TENURE, tenure);

	}

	public String getCardBinType() {
		return getParameter(PaymentUIUtil.CARD_BIN_TYPE);
	}
	public boolean isPhonePeAndroidFlow() {
		return "true".equals(getParameter(PaymentUIUtil.PHONEPE_ANDROID_FLOW));
	}
	public boolean isCredAppFlow() {
		return getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CRED) 
				&& (getProductCode().contains("android") || getProductCode().contains("ios"));
	}
	public String getPhonePeVersioncode() {
		return getParameter(PaymentUIUtil.PHONEPE_VERSION_CODE);
	}
	public boolean isPhonePeSwitchFlow() {
		String productCode = getParameter(PaymentUIUtil.PRODUCT_CODE);
		if("phonepedom".equalsIgnoreCase(productCode)
				|| "phonepeint".equalsIgnoreCase(productCode)
				|| "phpdom_hotel".equalsIgnoreCase(productCode)
				|| "phpint_hotel".equalsIgnoreCase(productCode))
			return true;
		else return false;
	}

	public String getCashPoolUserId() {
		return cashPoolUserId;
	}

	public void setCashPoolUserId(String cashPoolUserId) {
		this.cashPoolUserId = cashPoolUserId;
	}

	public String getCashPoolCorpId() {
		return cashPoolCorpId;
	}

	public void setCashPoolCorpId(String cashPoolCorpId) {
		this.cashPoolCorpId = cashPoolCorpId;
	}

	
	public void validatePayNowRequest(){
		PaymentUIUtil uiUtil = new PaymentUIUtil();	
		Map<String,String> parameterMap = new HashMap<String,String>();
		String payop = getPaymentOption();
		parameterMap.put(PaymentUIUtil.SUPER_PNR,getSuperPnr());
		parameterMap.put("paymentOption",getPaymentOption());
		parameterMap.put("paymentType",getPaymentOption());
		parameterMap.put("product",getProductCode());//tripType
		//parameterMap.put("uuid",getBookingDetails().getUUID());
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,getPaymentMode());
		if (PaymentUIUtil.isCardBasedPayop(payop)) {
			parameterMap.put("cardBrand", getCardBrand());
			parameterMap.put(PaymentUIUtil.CARD_BIN, getCardBin());
			String cardNumber = this.paymentOptionParamMap.get("cno");
			if (StringUtils.isBlank(cardNumber)) {
				this.paymentOptionParamMap.get("cardNumber");
			}
			parameterMap.put(PaymentUIUtil.CARD_NO, cardNumber);
			parameterMap.put(PaymentUIUtil.CARD_SECURITY_CODE,(String) this.paymentOptionParamMap.get(PaymentUIUtil.CARD_SECURITY_CODE));
			parameterMap.put(PaymentUIUtil.CARD_EXP_MONTH,(String) this.paymentOptionParamMap.get(PaymentUIUtil.CARD_EXP_MONTH));
			parameterMap.put(PaymentUIUtil.CARD_EXP_YEAR,(String) this.paymentOptionParamMap.get(PaymentUIUtil.CARD_EXP_YEAR));
			parameterMap.put(PaymentUIUtil.CARD_HOLDER_NAME,(String) this.paymentOptionParamMap.get(PaymentUIUtil.CARD_HOLDER_NAME));
		}
		if("upi".equalsIgnoreCase(payop)) {
			parameterMap.put("payerVA", (String)this.paymentOptionParamMap.get("payerVA")); 
		}
		if("nb".equalsIgnoreCase(payop)) {
			parameterMap.put("prBank",(String)this.paymentOptionParamMap.get("prBank")); 
		}
	
		uiUtil.validSpecialCharachers(parameterMap);
		
	}
	private void setSsoTokenToRequestParameters(HttpServletRequest httpRequest){
		this.requestParameters.put(PaymentUIUtil.SSO_TOKEN,PaymentUIUtil.getSSOToken(httpRequest));
	}
	
	public String getPayerVA()
	{
		return this.paymentOptionParamMap.get("payerVA");
	}
	public void setPayerVA(String payerVA){
		this.paymentOptionParamMap.put("payerVA",payerVA);
	}
	public String getCardNumber() {
		
		String cardNumber = this.paymentOptionParamMap.get("cno");
		if(StringUtils.isBlank(cardNumber)){
			cardNumber = this.paymentOptionParamMap.get("cardNumber");
		}
		return cardNumber;
		
	}
}

