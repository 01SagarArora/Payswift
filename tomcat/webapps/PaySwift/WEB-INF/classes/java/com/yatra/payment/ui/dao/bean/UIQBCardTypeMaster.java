package com.yatra.payment.ui.dao.bean;

public class UIQBCardTypeMaster {

	private String name;
	private String code;
	private String cardTypeLabel;
	private String cvvLabel;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCardTypeLabel() {
		return cardTypeLabel;
	}

	public void setCardTypeLabel(String cardTypeLabel) {
		this.cardTypeLabel = cardTypeLabel;
	}

	public String getCvvLabel() {
		return cvvLabel;
	}

	public void setCvvLabel(String cvvLabel) {
		this.cvvLabel = cvvLabel;
	}

}
