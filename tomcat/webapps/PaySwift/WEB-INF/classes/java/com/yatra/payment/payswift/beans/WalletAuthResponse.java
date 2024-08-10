package com.yatra.payment.payswift.beans;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class WalletAuthResponse {

	private String amountInPaisa;
	private String redeemedEcash;
	private String responseCode;
	private String responseMsg;
	private String sourceId;
	private String status;
	private String transactionId;
	private String walletId;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String success) {
		this.status = success;
	}
	public String getAmountInPaisa() {
		return amountInPaisa;
	}
	public void setAmountInPaisa(String amountInPaisa) {
		this.amountInPaisa = amountInPaisa;
	}
	public String getRedeemedEcash() {
		return redeemedEcash;
	}
	public void setRedeemedEcash(String redeemedEcash) {
		this.redeemedEcash = redeemedEcash;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getWalletId() {
		return walletId;
	}
	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}
	
	
}
