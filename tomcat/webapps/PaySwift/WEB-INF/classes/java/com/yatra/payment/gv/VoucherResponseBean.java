package com.yatra.payment.gv;

import net.sf.json.JSONArray;

public class VoucherResponseBean {
	private int resCode;
	private String resMsg;
	private String status;
	private String authCode;
	private double amount;
	private JSONArray vouchers;
	private String displayMessage;
	
	public int getResCode() {
		return resCode;
	}
	
	public void setResCode(int resCode) {
		this.resCode = resCode;
	}
	
	public String getResMsg() {
		return resMsg;
	}
	
	public void setResMsg(String resMsg) {
		this.resMsg = resMsg;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getAuthCode() {
		return authCode;
	}
	
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
	
	public JSONArray getVouchers() {
		return vouchers;
	}
	
	public void setVouchers(JSONArray vouchers) {
		this.vouchers = vouchers;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDisplayMessage() {
		return displayMessage;
	}

	public void setDisplayMessage(String displayMessage) {
		this.displayMessage = displayMessage;
	}
}
