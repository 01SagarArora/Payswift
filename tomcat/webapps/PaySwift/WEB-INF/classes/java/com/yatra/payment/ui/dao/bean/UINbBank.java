package com.yatra.payment.ui.dao.bean;


public class UINbBank {

	private String displayName;
	private String bankCode;
	private String status;
	private Boolean preferred;
	private String imageURL;
	private String iframeEnabled;
	private Integer priority;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getPreferred() {
		return preferred;
	}

	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getIframeEnabled() {
		return iframeEnabled;
	}

	public void setIframeEnabled(String iframeEnabled) {
		this.iframeEnabled = iframeEnabled;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}
