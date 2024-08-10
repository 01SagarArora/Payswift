package com.yatra.payment.client.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class WalletRequest {
	
	private String amountInRupees;
	private String userEmail;
	private String walletId;
	private String superPNR;
	private String merchantCode;
	private String productCode;
	private String ttid;
	
	public String getAmountInRupees() {
		return amountInRupees;
	}
	public void setAmountInRupees(String amountInRupees) {
		this.amountInRupees = amountInRupees;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getWalletId() {
		return walletId;
	}
	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}
	public String getSuperPNR() {
		return superPNR;
	}
	public void setSuperPNR(String superPNR) {
		this.superPNR = superPNR;
	}
	public String getMerchantCode() {
		return merchantCode;
	}
	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	public String getTtid() {
		return ttid;
	}
	public void setTtid(String ttid) {
		this.ttid = ttid;
	}
}
