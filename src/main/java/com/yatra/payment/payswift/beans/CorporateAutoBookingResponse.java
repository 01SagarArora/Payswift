package com.yatra.payment.payswift.beans;

import java.util.HashMap;
import java.util.Map;

public class CorporateAutoBookingResponse {
	
	private boolean status = false ;
	private String msg ;
	private String displayMsg ;
	private Map<String,Object> params = new HashMap<>();
	private Map<String,String> paymentMethodErrors = new HashMap<>();
	
	public CorporateAutoBookingResponse(){}
	
	public CorporateAutoBookingResponse(String msg){
		this.msg = msg;
	}
	public CorporateAutoBookingResponse(String msg,String displayMsg){
		this.msg = msg;
		this.displayMsg = displayMsg;
	}
		
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public Map<String, String> getPaymentMethodErrors() {
		return paymentMethodErrors;
	}
	public void setPaymentMethodErrors(Map<String, String> paymentMethodErrors) {
		this.paymentMethodErrors = paymentMethodErrors;
	}

	public String getDisplayMsg() {
		return displayMsg;
	}

	public void setDisplayMsg(String displayMsg) {
		this.displayMsg = displayMsg;
	}

	@Override
	public String toString() {
		return "CorporateAutoBookingResponse [status=" + status + ", msg="
				+ msg + ", displayMsg=" + displayMsg + ", params=" + params
				+ ", paymentMethodErrors=" + paymentMethodErrors + "]";
	}

	
	
}
