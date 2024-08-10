package com.yatra.payment.payswift.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.platform.json.YTJSON;

import static com.yatra.payment.ui.enums.FailureCode.VALIDATION_FROM_LOB_FAIL_RESPONSE;
import static com.yatra.payment.ui.enums.FailureCode.VALIDATION_FROM_LOB_PROMO_FAILED;

public class LobValidationResponse extends YTJSON{
	private static final long serialVersionUID = 1L;

	JSONObject lobValidationResponseJSON ;
	ArrayList<String> promoErrorMessages;
	ArrayList<String> validationErrorMessages;
	ArrayList<String> priceValidationMessages;
	APIStatusBean statusBean;
	private final String rawResponse;
	private String dbId;

	public LobValidationResponse(String lobResponse) {
		super(lobResponse);
		lobValidationResponseJSON = this.getJsonObject();
		promoErrorMessages=new ArrayList<String>();
		validationErrorMessages = new ArrayList<String>();
		priceValidationMessages = new ArrayList<String>();
		this.rawResponse = lobResponse;
		this.dbId = "0";
		this.statusBean = new APIStatusBean();
		validateResponseBean();
	} 

	public LobValidationResponse(String lobResponse, FailureCode failureCode) {
		super(lobResponse);
		lobValidationResponseJSON = this.getJsonObject();
		promoErrorMessages=new ArrayList<String>();
		this.rawResponse = lobResponse;
		this.statusBean = new APIStatusBean(ResponseStatus.FAILURE, failureCode);
		this.dbId = "0";
	} 

	public String getSuccess(){
		if(lobValidationResponseJSON.containsKey("success")){
			return lobValidationResponseJSON.getString("success");
		}
		return null;
	}

	public boolean getPromoSuccess() {
		try {
			if (lobValidationResponseJSON.containsKey("promo") && !lobValidationResponseJSON.getJSONObject("promo").isNullObject()) {
				return Boolean.parseBoolean(lobValidationResponseJSON.getJSONObject("promo").optString("success"));
			}
		}catch(Exception e){}
		return true;
	}
	
	public boolean getLobValidationSuccess() {
		try {
			if (lobValidationResponseJSON.containsKey("lobValidation") && !lobValidationResponseJSON.getJSONObject("lobValidation").isNullObject()) {
				return Boolean.parseBoolean(lobValidationResponseJSON.getJSONObject("lobValidation").optString("success"));
			}
		}catch(Exception e){}
		return true;
	}
	
	public boolean getPriceValidationSuccess() {
		try {
			if (lobValidationResponseJSON.containsKey("priceValidation") && !lobValidationResponseJSON.getJSONObject("priceValidation").isNullObject()) {
				return Boolean.parseBoolean(lobValidationResponseJSON.getJSONObject("priceValidation").optString("success"));
			}
		}catch(Exception e){}
		return true;
	}

	public boolean getDiscountSuccess() {
		try {
			if (lobValidationResponseJSON.containsKey("discount") && !lobValidationResponseJSON.getJSONObject("discount").isNullObject()) {
				return Boolean.parseBoolean(lobValidationResponseJSON.getJSONObject("discount").optString("success"));
			}
		}catch(Exception e){}
		return true;
	}
	
	public String getChangePromoUrl() {
		if (lobValidationResponseJSON.containsKey("promo") && !lobValidationResponseJSON.getJSONObject("promo").isNullObject()) {
			return lobValidationResponseJSON.getJSONObject("promo").optString("changePromoUrl");
		}
		return null;
	}
	
	public String getLobValidationFailureUrl() {
		if (lobValidationResponseJSON.containsKey("lobValidation") && !lobValidationResponseJSON.getJSONObject("lobValidation").isNullObject()) {
			return lobValidationResponseJSON.getJSONObject("lobValidation").optString("validationFailureUrl");
		}
		return null;
	}
	
	public String getPriceValidationFailureUrl() {
		if (lobValidationResponseJSON.containsKey("priceValidation") && !lobValidationResponseJSON.getJSONObject("priceValidation").isNullObject()) {
			return lobValidationResponseJSON.getJSONObject("priceValidation").optString("validationFailureUrl");
		}
		return null;
	}
	
	public List<String> getPromoMessages(){
		if (lobValidationResponseJSON.containsKey("promo") && (lobValidationResponseJSON.getJSONObject("promo").containsKey("messages"))) {
			JSONArray promoMessages = lobValidationResponseJSON.getJSONObject("promo").getJSONArray("messages");
			for(int i=0;i<promoMessages.size();i++){
				promoErrorMessages.add(promoMessages.getString(i));
			}
		}
		return promoErrorMessages;
	}
	
	public List<String> getLobValidationErrorMessages(){
		if (lobValidationResponseJSON.containsKey("lobValidation") && (lobValidationResponseJSON.getJSONObject("lobValidation").containsKey("messages"))) {
			JSONArray lobValidationErrorMessages = lobValidationResponseJSON.getJSONObject("lobValidation").getJSONArray("messages");
			for(int i=0;i<lobValidationErrorMessages.size();i++){
				validationErrorMessages.add(lobValidationErrorMessages.getString(i));
			}
		}
		return validationErrorMessages;
	}
	
	public List<String> getPriceValidationErrorMessages(){
		if (lobValidationResponseJSON.containsKey("priceValidation") && (lobValidationResponseJSON.getJSONObject("priceValidation").containsKey("messages"))) {
			JSONArray priceValidationErrorMessages = lobValidationResponseJSON.getJSONObject("priceValidation").getJSONArray("messages");
			for(int i=0;i<priceValidationErrorMessages.size();i++){
				priceValidationMessages.add(priceValidationErrorMessages.getString(i));
			}
		}
		return priceValidationMessages;
	}

	public String getPromoWarning(){		
		if (lobValidationResponseJSON.containsKey("promo") && (lobValidationResponseJSON.getJSONObject("promo").containsKey("warn"))) {
			return lobValidationResponseJSON.getJSONObject("promo").getString("warn");
		}

		return null;
	}
	
	public boolean getShowGSTMessage(){		
		if (lobValidationResponseJSON.containsKey("promo") && (lobValidationResponseJSON.getJSONObject("promo").containsKey("showGSTMessage"))) {
			return Boolean.parseBoolean(lobValidationResponseJSON.getJSONObject("promo").getString("showGSTMessage"));
		}

		return false;
	}

	public String getPromoAmount(){		
		if (lobValidationResponseJSON.containsKey("promo") && (lobValidationResponseJSON.getJSONObject("promo").containsKey("amount"))) {
			return lobValidationResponseJSON.getJSONObject("promo").getString("amount");
		}

		return null;
	}


	public boolean shouldRedirect() {
		try {
			if (lobValidationResponseJSON.containsKey("redirect")) {
				String redirect = lobValidationResponseJSON.getString("redirect");
				return Boolean.parseBoolean(redirect);
			}
		}catch (Exception e) {}
		return true;
	}

	public String getRurl(){
		if(lobValidationResponseJSON.containsKey("rurl")){
			return lobValidationResponseJSON.getString("rurl");
		}
		return null;
	}
	
	public String getFullIxigoFailureUrl(){
		if(lobValidationResponseJSON.containsKey("fullIxigoMoneyFailureUrl")){
			return lobValidationResponseJSON.getString("fullIxigoMoneyFailureUrl");
		}
		return null;
	}

	public String getTtid(){
		if(lobValidationResponseJSON.containsKey("ttid")){
			return lobValidationResponseJSON.getString("ttid");
		}
		return null;
	}

	public String getAmountToCollect(){
		if(lobValidationResponseJSON.containsKey("amountToCollect")){
			return lobValidationResponseJSON.getString("amountToCollect");
		}
		return null;
	}

	public String getS2Surl(){
		if(lobValidationResponseJSON.containsKey("s2sUrl")){
			return lobValidationResponseJSON.getString("s2sUrl");
		}
		return null;
	}

	public String getS2S(){
		if(lobValidationResponseJSON.containsKey("s2s")){
			return lobValidationResponseJSON.getString("s2s");
		}
		return null;
	}

	public String getS2SOnFailure(){
		if(lobValidationResponseJSON.containsKey("s2sOnFailure")){
			return lobValidationResponseJSON.getString("s2sOnFailure");
		}
		return null;
	}

	public String getRurlMethodType(){
		if(lobValidationResponseJSON.containsKey("rurlMethodType")){
			return lobValidationResponseJSON.getString("rurlMethodType");
		}
		return null;
	}

	public boolean isSuccess(){
		try{
			String success = lobValidationResponseJSON.getString("success");
			return Boolean.parseBoolean(success);
		}catch(Exception e){}
		return false;
	}

	public String getErrorMessage(){
		Map<String, String> errorParamMap = getErrorParamMap();
		if(errorParamMap!=null){
			return errorParamMap.get("message");
		}
		return "";
	}
	public Map<String, String> getErrorParamMap() {
		Map<String, String> errorParams = null;
		try{
			if (lobValidationResponseJSON.containsKey("errorParam")) {
				errorParams = new HashMap<String, String>();
				JSONObject jsonObject = lobValidationResponseJSON.getJSONObject("errorParam");
				return toMap(jsonObject);
			}
		}catch(Exception e){
		}
		return errorParams;
	}

	public  Map<String, String> toMap(JSONObject object)  {
		Map<String, String> map = new HashMap<String, String>();

		Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			String value = (String)object.get(key);
			map.put(key, value);
		}
		return map;
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
	
	public void setDBId(String dbId) {
		this.dbId = dbId;
	}

	public String getRawResponse() {
		return rawResponse;
	}

	private void validateResponseBean(){

		if(!isSuccess()){
			this.statusBean.setFailureCode((getPromoSuccess() == false? VALIDATION_FROM_LOB_PROMO_FAILED: VALIDATION_FROM_LOB_FAIL_RESPONSE));
			return;
		}

		/* Required parameters check can be added in following way 

		char separator = ';';
		StringBuilder missingParamsSB = new StringBuilder("");
		if(YatraUtil.isNullOrEmpty(getTtid())){
			missingParamsSB.append("ttid").append(separator);
		}

		if(missingParamsSB.length() > 0){
			this.statusBean.setFailureCode(FailureCode.VALIDATION_FROM_LOB_REQUIRED_PARAMS_MISSING.setCustomCode(missingParamsSB.toString()));
		}
		 */
	}

	public String getIsZeroAmountTxn(){
		if(lobValidationResponseJSON.containsKey("isZeroAmountTxn")){
			return lobValidationResponseJSON.getString("isZeroAmountTxn");
		}
		return "false";
	}
	
	public String getActualBookingAmount(){
		if(lobValidationResponseJSON.containsKey("actualBookingAmount")){
			return lobValidationResponseJSON.getString("actualBookingAmount");
		}
		return null;
	}

	public String getDbId() {
		return dbId;
	}
	
	// the value is reversed
	public boolean getPrimeWarning(){		
		if (lobValidationResponseJSON.containsKey("pmUCSuccess")) {
			String pmUCSuccess = lobValidationResponseJSON.getString("pmUCSuccess");
			return !Boolean.parseBoolean(pmUCSuccess);
		}
		return false;
	}
	
	public String getPricingURL()
	{
		if (lobValidationResponseJSON.containsKey("pricingURL")) {
			return lobValidationResponseJSON.getString("pricingURL");
		}
		return "";
	}
	
	public String getPrimeErrorMessage()
	{
		if (lobValidationResponseJSON.containsKey("pmUCMessage")) {
			return lobValidationResponseJSON.getString("pmUCMessage");
		}
		return "";
	}

}
