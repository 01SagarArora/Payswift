package com.yatra.payment.ui.beans;


import net.sf.json.JSONObject;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.json.YTJSON;

public class LobDetails extends YTJSON {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5864745984427273831L;
	JSONObject lobDetailsJSON ;
	public LobDetails(String lobDetails) {
		super(lobDetails);
		lobDetailsJSON = this.getJsonObject();
	}
	

	public String getItineraryDetailsHTML(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.ITINERARY_HTML)){
			return lobDetailsJSON.getString(PaymentUIUtil.ITINERARY_HTML);
		}
		return null;
	}
	
	public String getFareBreakUpJson(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.FARE_BREAKUP_JSON_AS_STRING)){
			return lobDetailsJSON.getString(PaymentUIUtil.FARE_BREAKUP_JSON_AS_STRING);
		}
		return null;
	}
	
	public String getUserEmail(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.USER_DETAILS)&&(!lobDetailsJSON.containsKey(PaymentUIUtil.EMAIL))){
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.USER_DETAILS).getString(PaymentUIUtil.EMAIL);
		}
		return null;
	}
	
	public String getUserMobile(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.USER_DETAILS)&&(!lobDetailsJSON.containsKey(PaymentUIUtil.MOBILE))){
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.USER_DETAILS).getString(PaymentUIUtil.MOBILE);
		}
		return null;
	}
	public String getMaximumRedeemableECash(){
		String maxRedeemableEcash = "0.0";
		if (lobDetailsJSON.containsKey(PaymentUIUtil.MAX_YLP)) {
			maxRedeemableEcash = lobDetailsJSON.getString(PaymentUIUtil.MAX_YLP);
		}
		else if(lobDetailsJSON.containsKey(PaymentUIUtil.WALLET_DETAILS)){
			JSONObject walletDetails = lobDetailsJSON.getJSONObject(PaymentUIUtil.WALLET_DETAILS);
			maxRedeemableEcash = walletDetails.getString(PaymentUIUtil.MAX_YLP);
		}
		return maxRedeemableEcash;
	}
	

}
