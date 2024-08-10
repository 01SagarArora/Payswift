package com.yatra.payment.ui.display.beans;

import com.yatra.payment.ui.enums.Status;

public class CardType {

	private String code;
	private String logoURL;
	private Status status;	
	private CardDetail cardDetail;
	private CardValidations cardValidations;
	
	
	public CardType() {}
	
	public CardType(CardType cardType) {
		this.code = cardType.getCode();
		this.logoURL = cardType.getLogoURL();
		this.status = cardType.getStatus();
		this.cardDetail = cardType.getCardDetail();
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
	
	public CardDetail getCardDetail() {
		return cardDetail;
	}
	
	public void setCardDetail(CardDetail cardDetail) {
		this.cardDetail = cardDetail;
	}
	
	public CardValidations getCardValidations() {
		return cardValidations;
	}

	public void setCardValidations(CardValidations cardValidations) {
		this.cardValidations = cardValidations;
	}

	@Override
	public String toString() {
		return "CardType [code=" + code
				+ ", logoURL=" + logoURL + ", status=" + status
				+ ", cardDetail="
				+ cardDetail + ", cardVlidations="+ cardValidations +"]";
	}
}
