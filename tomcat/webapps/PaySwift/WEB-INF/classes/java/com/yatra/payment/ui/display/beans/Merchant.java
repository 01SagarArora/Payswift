package com.yatra.payment.ui.display.beans;

/**
 * 
 * @author meenakshi
 *
 */
public class Merchant {
	
	private String merchantCode;
	private String merchantName;
	private String productCode;
	private String productName;
	private String responseUrl;
	private String requestChecksum;
	private String responseChecksum;
	private String logoUrl;
	
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
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getResponseUrl() {
		return responseUrl;
	}
	public void setResponseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
	}
	public String getRequestChecksum() {
		return requestChecksum;
	}
	public void setRequestChecksum(String requestChecksum) {
		this.requestChecksum = requestChecksum;
	}
	public String getResponseChecksum() {
		return responseChecksum;
	}
	public void setResponseChecksum(String responseChecksum) {
		this.responseChecksum = responseChecksum;
	}
	
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	@Override
	public String toString() {
		return "Merchant [merchantCode=" + merchantCode
		+ ", productCode=" + productCode + "]";
	}
}
