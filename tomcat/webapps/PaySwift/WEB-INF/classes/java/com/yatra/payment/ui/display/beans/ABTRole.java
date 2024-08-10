package com.yatra.payment.ui.display.beans;

public class ABTRole {

	private String displayName;
	private String code;

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

	@Override
	public String toString() {
		return "ABTRole [displayName=" + displayName + ", code=" + code + "]";
	}

}
