package com.yatra.payment.ui.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.yatra.payment.ui.display.beans.QuickBookCard;

public class QuickBook {
	
	private List<QuickBookCard> savedCards;
	private String errorCode;
	private String errorMsg;
	
	public QuickBook(List<QuickBookCard> savedCards, String errorCode, String errorMsg) {
		this.savedCards = savedCards;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public List<QuickBookCard> getSavedCards() {
		return savedCards;
	}
	
	public void setSavedCards(List<QuickBookCard> savedCards) {
		this.savedCards = savedCards;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
