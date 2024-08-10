package com.yatra.payment.ui.dao.bean;


public class UIPayop {

	private String displayName;
	private String code;
	private Integer priority;
	private String iframeEnabled;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getIframeEnabled() {
		return iframeEnabled;
	}

	public void setIframeEnabled(String iframeEnabled) {
		this.iframeEnabled = iframeEnabled;
	}

}
