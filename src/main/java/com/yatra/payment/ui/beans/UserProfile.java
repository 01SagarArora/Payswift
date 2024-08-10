package com.yatra.payment.ui.beans;

import org.apache.commons.lang.builder.ToStringBuilder;


public class UserProfile {
	
	private String emailId;
	private Boolean isAuthenticationNeeded;
	private Boolean isPasswordCompromised;
	private String authMode;
	private EWallet eWallet;
	private QuickBook quickBook;
	private String errorCode;
	private String errorMessage;
	
	public String getEmailId() {
		return emailId;
	}
	
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	
	public Boolean getIsAuthenticationNeeded() {
		return isAuthenticationNeeded;
	}
	
	public void setIsAuthenticationNeeded(Boolean isAuthenticationNeeded) {
		this.isAuthenticationNeeded = isAuthenticationNeeded;
	}
	
	public Boolean getIsPasswordCompromised() {
		return isPasswordCompromised;
	}
	
	public void setIsPasswordCompromised(Boolean isPasswordCompromised) {
		this.isPasswordCompromised = isPasswordCompromised;
	}
	
	public String getAuthMode() {
		return authMode;
	}
	
	public void setAuthMode(String authMode) {
		this.authMode = authMode;
	}
	
	public EWallet geteWallet() {
		return eWallet;
	}
	
	public void seteWallet(EWallet eWallet) {
		this.eWallet = eWallet;
	}
	
	public QuickBook getQuickBook() {
		return quickBook;
	}
	
	public void setQuickBook(QuickBook quickBook) {
		this.quickBook = quickBook;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
