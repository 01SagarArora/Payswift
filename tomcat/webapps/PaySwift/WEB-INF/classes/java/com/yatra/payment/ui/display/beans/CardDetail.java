package com.yatra.payment.ui.display.beans;

public class CardDetail {

	
	private String cardNoLength;
	private String expiryDateLabel;
	private String expiryMonth;
	private String expiryYear;
	private String cvvLabel;
	private String cvvLength;
	private String cvvImageURL;
	private String cvvImageText;
	private String withoutCVVSupported;

	public String getCardNoLength() {
		return cardNoLength;
	}

	public void setCardNoLength(String cardNoLength) {
		this.cardNoLength = cardNoLength;
	}

	public String getExpiryDateLabel() {
		return expiryDateLabel;
	}

	public void setExpiryDateLabel(String expiryDateLabel) {
		this.expiryDateLabel = expiryDateLabel;
	}

	public String getExpiryMonth() {
		return expiryMonth;
	}

	public void setExpiryMonth(String expiryMonth) {
		this.expiryMonth = expiryMonth;
	}

	public String getExpiryYear() {
		return expiryYear;
	}

	public void setExpiryYear(String expiryYear) {
		this.expiryYear = expiryYear;
	}

	public String getCvvLabel() {
		return cvvLabel;
	}

	public void setCvvLabel(String cvvLabel) {
		this.cvvLabel = cvvLabel;
	}

	public String getCvvLength() {
		return cvvLength;
	}

	public void setCvvLength(String cvvLength) {
		this.cvvLength = cvvLength;
	}

	public String getCvvImageURL() {
		return cvvImageURL;
	}

	public void setCvvImageURL(String cvvImageURL) {
		this.cvvImageURL = cvvImageURL;
	}

	public String getCvvImageText() {
		return cvvImageText;
	}

	public void setCvvImageText(String cvvImageText) {
		this.cvvImageText = cvvImageText;
	}

	public String getWithoutCVVSupported() {
		return withoutCVVSupported;
	}

	public void setWithoutCVVSupported(String withoutCVVSupported) {
		this.withoutCVVSupported = withoutCVVSupported;
	}

	@Override
	public String toString() {
		return "CardDetail [cardNoLength=" + cardNoLength + ",expiryDateLabel=" + expiryDateLabel + ", expiryMonth=" + expiryMonth
				+ ", expiryYear=" + expiryYear + ", cvvLabel=" + cvvLabel + ", cvvLength=" + cvvLength + 
				  ", cvvImageURL=" + cvvImageURL + ", cvvImageText=" + cvvImageText + ", withoutCVVSupported=" + withoutCVVSupported + "]";
	}
}
