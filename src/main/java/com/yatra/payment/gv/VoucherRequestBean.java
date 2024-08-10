package com.yatra.payment.gv;

import net.sf.json.JSONArray;

public class VoucherRequestBean {
	private String superPnr;
	private String emailId;
	private String ssoToken;
	private String cardHash;
	private String amount;
	private String lob;
	private String bin;
	private String isd;
	private String mobile;
	private String source;
	private String context;
	private String vendorName;
	private String authCode;
	private String payop;
	private int voucherLimitTo;
	private JSONArray vouchers;
	private String flightClass;
	private String tripType;
	private String totPax;
	
	public String getSuperPnr() {
		return superPnr;
	}

	public void setSuperPnr(String superPnr) {
		this.superPnr = superPnr;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getSsoToken() {
		return ssoToken;
	}

	public void setSsoToken(String ssoToken) {
		this.ssoToken = ssoToken;
	}

	public String getCardHash() {
		return cardHash;
	}

	public void setCardHash(String cardHash) {
		this.cardHash = cardHash;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getLob() {
		return lob;
	}

	public void setLob(String lob) {
		this.lob = lob;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getIsd() {
		return isd;
	}

	public void setIsd(String isd) {
		this.isd = isd;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public JSONArray getVouchers() {
		return vouchers;
	}

	public void setVouchers(JSONArray vouchers) {
		this.vouchers = vouchers;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getPayop() {
		return payop;
	}

	public void setPayop(String payop) {
		this.payop = payop;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public int getVoucherLimitTo() {
		return voucherLimitTo;
	}

	public void setVoucherLimitTo(int voucherLimitTo) {
		this.voucherLimitTo = voucherLimitTo;
	}

	public String getFlightClass() {
		return flightClass;
	}

	public void setFlightClass(String flightClass) {
		this.flightClass = flightClass;
	}

	public String getTripType() {
		return tripType;
	}

	public void setTripType(String tripType) {
		this.tripType = tripType;
	}

	public String getTotPax() {
		return totPax;
	}

	public void setTotPax(String totPax) {
		this.totPax = totPax;
	}
	
	@Override
	public String toString() {
		return "VoucherRequestBean [superPnr=" + superPnr + ", emailId=" + emailId + ", ssoToken=" + ssoToken
				+ ", cardHash=" + cardHash + ", amount=" + amount + ", lob=" + lob + ", bin=" + bin + ", isd=" + isd
				+ ", mobile=" + mobile + ", source=" + source + ", context=" + context + ", authCode=" + authCode
				+ ", vendorName=" + vendorName + ", vouchers=" + vouchers + ", voucherLimitTo=" + voucherLimitTo + 
				", flightClass=" + flightClass + ", tripType=" + tripType + ", totPax=" + totPax + "]";
	}

}
