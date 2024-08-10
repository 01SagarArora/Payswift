package com.yatra.payment.ui.dao.bean;


public class UIPayopSuboptionMapping implements Comparable<UIPayopSuboptionMapping> {

	private String paymentOption;
	private String subOption;
	private String displayName;
	private String imageURL;
	private String status;
	private String merchantCode;
	private String productCode;
	private int priority;
	

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getSubOption() {
		return subOption;
	}

	public void setSubOption(String subOption) {
		this.subOption = subOption;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentOption() {
		return paymentOption;
	}

	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
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
	public int compareTo(UIPayopSuboptionMapping other) {
		return this.priority - other.priority;
	}
	
	

}
