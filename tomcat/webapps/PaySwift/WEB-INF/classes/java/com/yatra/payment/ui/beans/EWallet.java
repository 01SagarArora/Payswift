package com.yatra.payment.ui.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.wallet.beans.resp.GetECashRS.ECash;

public class EWallet {
	
	private ECash totalECash;
	private List<ECash> eCashList;
	private String errorCode;
	private String errorMsg;
	
	public EWallet(ECash totalECash, List<ECash> eCashList, String errorCode, String errorMsg) {
		this.totalECash = totalECash;
		this.eCashList = eCashList;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public ECash getTotalECash() {
		return totalECash;
	}
	
	public void setTotalECash(ECash totalECash) {
		this.totalECash = totalECash;
	}
	
	public List<ECash> geteCashList() {
		return eCashList;
	}
	
	public void seteCashList(List<ECash> eCashList) {
		this.eCashList = eCashList;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
