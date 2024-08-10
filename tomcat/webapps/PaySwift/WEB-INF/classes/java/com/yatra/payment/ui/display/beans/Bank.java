package com.yatra.payment.ui.display.beans;

import com.yatra.payment.ui.enums.Status;

public class Bank {
	
	private String displayText;
	private String code;
	private Status status;
	private Boolean preferred;
	private String logoURL;
	private String iframeEnabled;
	private Integer priority;
        
	public Bank() {}
	
	public Bank(Bank bank) {
		this.displayText = bank.getDisplayText();
		this.code = bank.getCode();
		this.status = bank.getStatus();
		this.preferred = bank.getPreferred();
		this.logoURL = bank.getLogoURL();
	}
	
	public String getDisplayText() {
		return displayText;
	}
	
	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public Boolean getPreferred() {
		return preferred;
	}

	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}

	public String getLogoURL() {
		return logoURL;
	}

	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}

	@Override
	public String toString() {
		return "Bank [displayText=" + displayText + ", code=" + code
				 + ",preferred=" + preferred + ",logoURL=" + logoURL + ", status=" + status + "]";
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
