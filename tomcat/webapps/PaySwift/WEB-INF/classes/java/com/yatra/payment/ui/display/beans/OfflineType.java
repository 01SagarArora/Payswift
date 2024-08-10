package com.yatra.payment.ui.display.beans;

import com.yatra.payment.ui.enums.Status;

public class OfflineType {
	
	private String optionCode;
	private String displayName;
	private Status status;
	
	public String getOptionCode() {
		return optionCode;
	}
	public void setOptionCode(String optionCode) {
		this.optionCode = optionCode;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "OfflineType [optionCode=" + optionCode + ", displayName=" + displayName + ", status=" + status + "]";
	}
}
