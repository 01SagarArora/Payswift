package com.yatra.payment.payswift.beans;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.yatra.platform.commons.role.CallContextKeeper;
import com.yatra.platform.util.YatraUtil;

public class PaymentInfoBean {

	String superPnr;
	String ssoToken;
	String versionNo;
	String productCode;
	String merchantCode;
	JSONObject responseObj;
	String client;
	String uuid;
	Boolean status;
	Boolean loggedInStatus;
	String additionalPayment;
	String userEmail;
	String promoCode;
	String isPromoApplied;
	String isBookingDetailsRequired;
	String bookingDetailAPIEndpoint;
	String mobile;
	String totalAmount;
	String appVersion;


	public PaymentInfoBean(){}

	public PaymentInfoBean(HttpServletRequest httpRequest){
		this.appVersion = httpRequest.getParameter("appVersion");
		this.superPnr = httpRequest.getParameter("superPnr");
		this.versionNo = httpRequest.getParameter("version");
		this.productCode = httpRequest.getParameter("productCode");
		this.merchantCode = httpRequest.getParameter("merchantCode");
		this.ssoToken = CallContextKeeper.getCallContext().getSsoToken();
		this.client = httpRequest.getParameter("client");
		this.uuid = httpRequest.getParameter("uuid");
		this.responseObj = new JSONObject();
		this.status = true;
		this.additionalPayment = httpRequest.getParameter("additionalPayment");
		this.userEmail = httpRequest.getParameter("userEmail");
		this.isPromoApplied = httpRequest.getParameter("isPromoApplied");
		this.isBookingDetailsRequired = httpRequest.getParameter("isBookingDetailsRequired");
		this.bookingDetailAPIEndpoint = httpRequest.getParameter("detailAPIEndpoint");
		if(this.versionNo==null)
			versionNo = "";
		if(this.uuid==null)
			uuid= "";
		
		if(YatraUtil.isNullOrEmpty(this.ssoToken))
			this.ssoToken = httpRequest.getParameter("ssoToken"); // As discussed
		
		if(YatraUtil.isNullOrEmpty(this.ssoToken))
			this.loggedInStatus= false;
		else
			this.loggedInStatus= true;
	}
	
	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

	public String getIsPromoApplied() {
		return isPromoApplied;
	}

	public void setIsPromoApplied(String isPromoApplied) {
		this.isPromoApplied = isPromoApplied;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Boolean getLoggedInStatus() {
		return loggedInStatus;
	}

	public void setLoggedInStatus(Boolean loggedInStatus) {
		this.loggedInStatus = loggedInStatus;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public JSONObject getResponseObj() {
		return responseObj;
	}

	public void setResponseObj(JSONObject responseObj) {
		this.responseObj = responseObj;
	}

	public String getSuperPnr() {
		return superPnr;
	}
	public void setSuperPnr(String superPnr) {
		this.superPnr = superPnr;
	}
	public String getSsoToken() {
		return ssoToken;
	}
	public void setSsoToken(String ssoToken) {
		this.ssoToken = ssoToken;
	}
	public String getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(String versionNo) {
		this.versionNo = versionNo;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getMerchantCode() {
		return merchantCode;
	}
	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}
	public String getAdditionalPayment() {
		return additionalPayment;
	}

	public void setAdditionalPayment(String additionalPayment) {
		this.additionalPayment = additionalPayment;
	}


	public String getUserEmail() {
		return userEmail;
	}
	
	public String getIsBookingDetailsRequired() {
		return isBookingDetailsRequired;
	}
	
	public String getBookingDetailAPIEndpoint() {
		return bookingDetailAPIEndpoint;
	}
		
	public void setBookingDetailAPIEndpoint(String bookingDetailAPIEndpoint) {
		this.bookingDetailAPIEndpoint = bookingDetailAPIEndpoint;
	}
	
	@Override
	public String toString() {
		return "PaymentInfoBean [superPnr=" + superPnr + ", ssoToken="
				+ ssoToken + ", versionNo=" + versionNo + ", productCode="
				+ productCode + ", merchantCode=" + merchantCode
				+ ", responseObj=" + responseObj + ", client=" + client
				+ ", uuid=" + uuid + ", status=" + status + ", loggedInStatus="
				+ loggedInStatus + ", additionalPayment=" + additionalPayment
				+ ", userEmail=" + userEmail + ",promoCode = "+ promoCode + ",appVersion = "+ appVersion + ", isBookingDetailsRequired=" + isBookingDetailsRequired + ", bookingDetailAPIEndpoint=" + bookingDetailAPIEndpoint + "]";
	}

	public void setMobile(String userMobile) {
		this.mobile	= userMobile;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getMobile() {
		return mobile;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
}
