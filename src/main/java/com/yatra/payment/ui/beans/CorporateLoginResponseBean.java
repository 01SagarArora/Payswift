package com.yatra.payment.ui.beans;

public class CorporateLoginResponseBean {
	private String status ;
	private String tokenId;
	private String message;
	
	public CorporateLoginResponseBean(String status, String tokenId, String message){
		this.message = message;
		this.tokenId = tokenId;
		this.status = status;
	}
	
	public String getTokenId() {
		return tokenId;
	}
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
