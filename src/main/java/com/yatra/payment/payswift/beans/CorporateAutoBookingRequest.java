package com.yatra.payment.payswift.beans;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.yatra.payment.ui.util.PaymentUIUtil;

public class CorporateAutoBookingRequest {
	
	private String merchant;
	private String product;
	private String superPnr;
	private String ssoToken;
	private String email;
	private String productType;
	private String bookingType;
	private String promoCode;
	private String ctripId;

	private String uuid;

	private String txnId;
	
	public CorporateAutoBookingRequest() {}
	
	public CorporateAutoBookingRequest(HttpServletRequest request) {
		setProduct(request.getParameter(PaymentUIUtil.PRODUCT_CODE));
		setSuperPnr(request.getParameter(PaymentUIUtil.SUPER_PNR));
		setSsoToken(request.getParameter(PaymentUIUtil.SSO_TOKEN));
		setEmail(request.getParameter(PaymentUIUtil.EMAIL));
		setProductType(request.getParameter(PaymentUIUtil.PRODUCT_TYPE));
		setBookingType(request.getParameter(PaymentUIUtil.BOOKING_TYPE));
		setUuid(request.getParameter(PaymentUIUtil.UUID));
		setTxnId(request.getParameter(PaymentUIUtil.TXN_ID));
		String merchant = request.getParameter(PaymentUIUtil.MERCHANT_CODE);
		if(StringUtils.isBlank(merchant)){
			merchant = PaymentUIUtil.DEFAULT_YATRA_MERCHANT;
		}
		setMerchant(merchant);
	}
	
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getBookingType() {
		return bookingType;
	}

	public void setBookingType(String bookingType) {
		this.bookingType = bookingType;
	}

	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

	public String getCorprateTripId() {
		return ctripId;
	}

	public void setCorporateTripId(String ctripId) {
		this.ctripId = ctripId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
}
