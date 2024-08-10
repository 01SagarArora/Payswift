package com.yatra.payment.ui.display.beans;

import com.yatra.payment.ui.enums.Status;

/**
 * 
 * @author meenakshi
 *
 */
public class CashCard {
	
	private String displayText;
	private String code;
	private String logoURL;
	private Status status;
	
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
	public String getLogoURL() {
		return logoURL;
	}
	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "CashCard [displayText=" + displayText + ", code=" + code
		+ ",logoURL=" + logoURL + ", status=" + status + "]";
	}
	
	

}
