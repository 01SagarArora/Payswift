package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.payment.ui.enums.Status;

public class QuickBookPaymentOption extends PaymentOption {
	
	private Status saveCard;
	private String authMode;
	private boolean authenticationRequired;
	private boolean authenticationFailed;
	private List<QuickBookCard> quickBookCards;
	private String errorCode;
	private String errorMesaage;
	private boolean hasSavedCards;
	
	public QuickBookPaymentOption() {}
	
	public QuickBookPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof QuickBookPaymentOption) {
			QuickBookPaymentOption quickBookPaymentOption = (QuickBookPaymentOption) paymentOption;
			this.saveCard = quickBookPaymentOption.getSaveCard();
			this.authMode = quickBookPaymentOption.getAuthMode();
			this.authenticationRequired = quickBookPaymentOption.isAuthenticationRequired();
			this.authenticationFailed = quickBookPaymentOption.isAuthenticationFailed();
			this.quickBookCards = quickBookPaymentOption.getQuickBookCards();
			this.errorCode = quickBookPaymentOption.getErrorCode();
		}
	}
	
	public List<QuickBookCard> getQuickBookCards() {
		return quickBookCards;
	}
	
	public void setQuickBookCards(List<QuickBookCard> quickBookCards) {
		this.quickBookCards = quickBookCards;
	}
	
	public Status getSaveCard() {
		return saveCard;
	}
	
	public void setSaveCard(Status saveCard) {
		this.saveCard = saveCard;
	}
	
	public String getAuthMode() {
		return authMode;
	}
	
	public void setAuthMode(String authMode) {
		this.authMode = authMode;
	}
	
	public boolean isAuthenticationRequired() {
		return authenticationRequired;
	}
	
	public void setAuthenticationRequired(boolean authenticationRequired) {
		this.authenticationRequired = authenticationRequired;
	}
	
	public boolean isAuthenticationFailed() {
		return authenticationFailed;
	}
	
	public void setAuthenticationFailed(boolean authenticationFailed) {
		this.authenticationFailed = authenticationFailed;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorMesaage() {
		return errorMesaage;
	}

	public void setErrorMesaage(String errorMesaage) {
		this.errorMesaage = errorMesaage;
	}

	public boolean hasSavedCards() {
		return hasSavedCards;
	}

	public void setHasSavedCards(boolean hasSavedCards) {
		this.hasSavedCards = hasSavedCards;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
