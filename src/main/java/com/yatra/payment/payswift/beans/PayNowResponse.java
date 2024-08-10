package com.yatra.payment.payswift.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PayNowResponse {
	
	private boolean success;
	private boolean redirect;
	private boolean ajax;
	private boolean isNoCostEMI;
	private boolean googlePay;
	private boolean cred;
	@JsonIgnore
	private ResponseDecisionScenarios responseDecision;
	@JsonIgnore
	private LobValidationResponse validationResponse;
	private String rurl;
	private String pollStatusUrl;
	private String message;
	private Map<String, String> redirectMap = new HashMap<String, String>();
	private Map<String, String> initPaymentMap;
	private Map<String, String> failRedirectMap ;
	private boolean promoSuccess = true;
	private String changePromoUrl;
	private boolean discountSuccess = true;
	private List<String> promoMessages;
	private String promoWarning;
	private String promoAmount;
	private String amountToCollect;
	private boolean priceValidationSuccess;
	private List<String> priceValidationMessage;
	private String priceValidationFailureUrl;
	private boolean lobValidationSuccess = true;
	private List<String> lobValidationMessage;
	private String lobValidationFailureUrl;
	private boolean showGSTMessage;
	@JsonIgnore
	private String ttid;
	@JsonIgnore
	private APIStatusBean statusBean = new APIStatusBean();
	@JsonIgnore
	private String agentCode;
	private String pollTransationUrl;
	private String pollIntervalInSec;
	private String pollTimeoutInSec;
	private String minkasuFlag;
	private String jusPayFlag;
	private String retNum;
	private boolean primeWarning;
	private String pricingUrl;
	private String primeErrorMessage;
	
	public boolean isSuccess() {
		return success;
	}
	public String getRurl() {
		return rurl;
	}
	public Map<String, String> getRedirectMap() {
		return redirectMap;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void setRurl(String rurl) {
		this.rurl = rurl;
	}
	public void putInPgData(String key, String value){
		redirectMap.put(key, value);
	}
	public void putInPgData(Map<String,String> pgData){
		if(pgData!=null)
		this.redirectMap.putAll(pgData);
	}
	public void putInRedirectMap(Map<String,String> pgData){
		if(pgData!=null)
		this.redirectMap = pgData;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Success :");
		sb.append(success);
		sb.append(" , ");
		sb.append("rurl :");
		sb.append(rurl);
		sb.append(" , ");
		sb.append("promoSuccess :");
		sb.append(promoSuccess);
		sb.append(" , ");
		sb.append("undoPromoUrl :");
		sb.append(changePromoUrl);
		sb.append(" , ");
		sb.append("discountSuccess :");
		sb.append(discountSuccess);
		sb.append(" , ");
		sb.append("minkasuFlag :");
		sb.append(minkasuFlag);
		sb.append(" , ");
		sb.append("jusPayFlag :");
		sb.append(jusPayFlag);
		sb.append(" , ");
		Map<String , String> parameterMap = redirectMap;
		for(String key : parameterMap.keySet()){
			if((key.equalsIgnoreCase("hk")||key.equalsIgnoreCase("cardNo")||key.equalsIgnoreCase("cno")||key.equalsIgnoreCase("ccsc")||
					key.equalsIgnoreCase("cexpm")||key.equalsIgnoreCase("cexpy"))){
				continue;
			}
			sb.append(key);
			sb.append(":");
			sb.append(parameterMap.get(key));
			sb.append(" , ");
		}
		return sb.toString();
	}
	@JsonIgnore
	public ResponseDecisionScenarios getResponseDecision() {
		return responseDecision;
	}
	@JsonIgnore
	public void setResponseDecision(ResponseDecisionScenarios responseDecision) {
		this.responseDecision = responseDecision;
	}
	@JsonIgnore
	public LobValidationResponse getValidationResponse() {
		return validationResponse;
	}
	@JsonIgnore
	public void setValidationResponse(LobValidationResponse validationResponse) {
		this.validationResponse = validationResponse;
	}
	public boolean isRedirect() {
		return redirect;
	}
	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}
	public boolean isAjax() {
		return ajax;
	}
	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}
	public boolean getPromoSuccess() {
		return promoSuccess;
	}

	public void setPromoSuccess() {
		this.promoSuccess = getValidationResponse().getPromoSuccess();
	}
	
	public boolean getLobValidationSuccess() {
		return lobValidationSuccess;
	}
	
	public boolean getPriceValidationSuccess() {
		return priceValidationSuccess;
	}
	
	public void setPriceValidationSuccess() {
		this.priceValidationSuccess = getValidationResponse().getPriceValidationSuccess();
	}
	
	public void setLobValidationSuccess() {
		this.lobValidationSuccess = getValidationResponse().getLobValidationSuccess();
	}

	public boolean getDiscountSuccess() {
		return discountSuccess;
	}

	public void setDiscountSuccess() {
		this.discountSuccess = getValidationResponse().getDiscountSuccess();
	}
	public String getChangePromoUrl() {
		return changePromoUrl;
	}
	public void setChangePromoUrl() {
		this.changePromoUrl = getValidationResponse().getChangePromoUrl();
	}
	public String getLobValidationFailureUrl() {
		return lobValidationFailureUrl;
	}
	public void setLobValidationFailureUrl() {
		this.lobValidationFailureUrl = getValidationResponse().getLobValidationFailureUrl();
	}
	public String getPriceValidationFailureUrl() {
		return priceValidationFailureUrl;
	}
	public void setPriceValidationFailureUrl() {
		this.priceValidationFailureUrl = getValidationResponse().getPriceValidationFailureUrl();
	}
	
	public void setPromoParameters() {
		setChangePromoUrl();
		setPromoSuccess();
		setPromoMessages();
		setPromoWarning();
		setPromoAmount();
		setAmountToCollect();
		setLobValidationFailureUrl();
		setLobValidationMessage();
		setLobValidationSuccess();
		setPriceValidationSuccess();
		setPriceValidationFailureUrl();
		setPriceValidationMessage();
		setShowGSTMessage();
		setPrimeWarning();
		setPricingUrl();
		setPrimeErrorMessage();
	}
	private void setPromoMessages() {
		this.promoMessages=getValidationResponse().getPromoMessages();
		
	}
	public List<String> getPromoMessages() {
		return promoMessages;
	}
	public List<String> getLobValidationMessage() {
		return lobValidationMessage;
	}
	public void setLobValidationMessage() {
		this.lobValidationMessage = getValidationResponse().getLobValidationErrorMessages();
	}
	public List<String> getPriceValidationMessage() {
		return priceValidationMessage;
	}
	public void setPriceValidationMessage() {
		this.priceValidationMessage = getValidationResponse().getPriceValidationErrorMessages();
	}
	public String getPromoWarning() {
		return promoWarning;
	}
	public void setPromoWarning() {
		this.promoWarning = getValidationResponse().getPromoWarning();
	}
	public String getPromoAmount() {
		return promoAmount;
	}
	public void setPromoAmount() {
		this.promoAmount = getValidationResponse().getPromoAmount();
	}
	public String getAmountToCollect() {
		return amountToCollect;
	}
	public void setAmountToCollect() {
		this.amountToCollect = getValidationResponse().getAmountToCollect();
	}
	public Map<String, String> getFailRedirectMap() {
		return failRedirectMap;
	}
	public void setFailRedirectMap(Map<String, String> failRedirectMap) {
		this.failRedirectMap = failRedirectMap;
	}
	public String getPollStatusUrl() {
		return pollStatusUrl;
	}
	public void setPollStatusUrl(String pollStatusUrl) {
		this.pollStatusUrl = pollStatusUrl;
	}
	public ResponseStatus getResponseStatus() {
		return this.statusBean.getResponseStatus();
	}
	public FailureCode getFailureCode() {
		return this.statusBean.getFailureCode();
	}
	public void setFailureCode(FailureCode failureCode) {
		this.statusBean.setFailureCode(failureCode);
	}
	public String getTtid() {
		return ttid;
	}
	public void setTtid(String ttid) {
		this.ttid = ttid;
	}
	public String getAgentCode() {
		return agentCode;
	}
	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}
	public Map<String, String> getInitPaymentMap() {
		return initPaymentMap;
	}
	public void setInitPaymentMap(Map<String, String> initPaymentMap) {
		this.initPaymentMap = initPaymentMap;
	}
	public boolean isGooglePay() {
		return googlePay;
	}
	public void setGooglePay(boolean googlePay) {
		this.googlePay = googlePay;
	}
	public boolean isCred() {
		return cred;
	}
	public void setCred(boolean cred) {
		this.cred = cred;
	}
	public boolean getShowGSTMessage() {
		return showGSTMessage;
	}
	public void setShowGSTMessage() {
		this.showGSTMessage = getValidationResponse().getShowGSTMessage();
	}
	public boolean isNoCostEMI() {
		return isNoCostEMI;
	}
	public void setNoCostEMI(boolean isNoCostEMI) {
		this.isNoCostEMI = isNoCostEMI;
	}
	public String getPollTransationUrl() {
	    return pollTransationUrl;
	}
	public String getPollIntervalInSec() {
	    return pollIntervalInSec;
	}
	public String getPollTimeoutInSec() {
	    return pollTimeoutInSec;
	}
	public void setPollTransationUrl(String pollTransationUrl) {
	    this.pollTransationUrl = pollTransationUrl;
	}
	public void setPollIntervalInSec(String pollIntervalInSec) {
	    this.pollIntervalInSec = pollIntervalInSec;
	}
	public void setPollTimeoutInSec(String pollTimeoutInSec) {
	    this.pollTimeoutInSec = pollTimeoutInSec;
	}
	public String getMinkasuFlag() {
		return minkasuFlag;
	}
	public void setMinkasuFlag(String minkasuFlag) {
		this.minkasuFlag = minkasuFlag;
	}
	public String getJusPayFlag() {
		return jusPayFlag;
	}
	public void setJusPayFlag(String jusPayFlag) {
		this.jusPayFlag = jusPayFlag;
	}
	public void setPrimeWarning() {
		primeWarning = getValidationResponse().getPrimeWarning();
	}
	public void setPricingUrl() {
		pricingUrl = getValidationResponse().getPricingURL();
	}
	public boolean isPrimeWarning() {
		return primeWarning;
	}
	public void setPrimeWarning(boolean primeWarning) {
		this.primeWarning = primeWarning;
	}
	public String getPricingUrl() {
		return pricingUrl;
	}
	public void setPricingUrl(String pricingUrl) {
		this.pricingUrl = pricingUrl;
	}
	public String getPrimeErrorMessage() {
		return primeErrorMessage;
	}
	public void setPrimeErrorMessage() {
		primeErrorMessage = getValidationResponse().getPrimeErrorMessage();
	}
}
