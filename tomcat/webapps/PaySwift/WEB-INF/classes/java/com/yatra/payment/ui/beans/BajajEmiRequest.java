package com.yatra.payment.ui.beans;

public class BajajEmiRequest {
	private String merchantCode;
	private String productCode;
	private String ttid;
	private String superPnr;
	private String tenure;
	private String cardNumber;
	private String amount;
	
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
	public String getTtid() {
		return ttid;
	}
	public void setTtid(String ttid) {
		this.ttid = ttid;
	}
	public String getSuperPnr() {
		return superPnr;
	}
	public void setSuperPnr(String superPnr) {
		this.superPnr = superPnr;
	}
	public String getTenure() {
		return tenure;
	}
	public void setTenure(String tenure) {
		this.tenure = tenure;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public BajajEmiRequest() {
		super();
	}
	
}

