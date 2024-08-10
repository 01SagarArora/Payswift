package com.yatra.payment.ui.display.beans;

import org.codehaus.jackson.annotate.JsonProperty;

public class QuickBookCard {
	
	private String cardId;
	private String cardTypeLabel;
	private String cardBrand;
	private String cardLogoURL;
	private String cardNumber;
	private String cvvLabel;
	private String cvvLength;
	private String cardAccessType;
	@JsonProperty("addressSaved")
	private boolean isAddressSaved;
	private String cardName;
	private String cardHolderFirstName;
	private String cardHolderLastName;
	private String expiryMonth;
	private String expiryYear;
	private String cardType;
	private boolean isGDSCard;
	private String bookingType;
	private String bankName;

	private String tokenRefID;
	private String tokenBin;
	private String tokenRefNo;

	@JsonProperty("isTokenized")
	private boolean tokenized;

	public QuickBookCard() {
		
	}
        
	public QuickBookCard(String cardTypeLabel, String cardBrand, String cardLogoURL, String cardNumber, String cvvLabel, String cvvLength, String cardId,String cardAccessType, boolean isAddressSaved, String cardName, String cardHolderFirstName, String cardHolderLastName, String expiryMonth, String expiryYear, String cardType, boolean isGDSCard, String bookingType, String cardBank){
		this.cardTypeLabel = cardTypeLabel;
		this.cardBrand = cardBrand;
		this.cardLogoURL = cardLogoURL;
		this.cardNumber = cardNumber;
		this.cvvLabel = cvvLabel;
		this.cvvLength = cvvLength;
		this.cardId = cardId;
		this.cardAccessType = cardAccessType;
		this.isAddressSaved = isAddressSaved;
		this.cardName = cardName;
		this.cardHolderFirstName = cardHolderFirstName;
		this.cardHolderLastName = cardHolderLastName;
		this.expiryMonth = expiryMonth;
		this.cardType = cardType;
		this.expiryYear = expiryYear;
		this.isGDSCard = isGDSCard;
		this.bookingType = bookingType;
		this.bankName= cardBank;
	}


	public QuickBookCard(String cardTypeLabel2, String cardBrand2, String cardLogoURL2, String cardNumber2,
						 String cvvLabel2, String cvvLength2, String cardId2, String cardAccessType2, boolean isAddressSaved2,
						 String cardType2, boolean isGDSCard2, String bookingType2) {
		this.cardTypeLabel = cardTypeLabel2;
		this.cardBrand = cardBrand2;
		this.cardLogoURL = cardLogoURL2;
		this.cardNumber = cardNumber2;
		this.cardId = cardId2;
		this.cardAccessType = cardAccessType2;
		this.isAddressSaved = isAddressSaved2;
		this.cardType = cardType2;
		this.isGDSCard = isGDSCard2;
		this.bookingType = bookingType2;
		this.cvvLabel = cvvLabel2;
		this.cvvLength= cvvLength2;


	}
	// Cons added to set tokenizedcardinfo

	public QuickBookCard(String cardTypeLabel2, String cardBrand2, String cardLogoURL2, String cardNumber2,
			String cvvLabel2, String cvvLength2, String cardId2, String cardAccessType2, boolean isAddressSaved2,
			String cardType2, boolean isGDSCard2, String bookingType2, String cardBank,String cardHolderFirstName,String cardHolderLastName ,String expiryMonth ,String expiryYear,boolean tokenized) {
		this.cardTypeLabel = cardTypeLabel2;
		this.cardBrand = cardBrand2;
		this.cardLogoURL = cardLogoURL2;
		this.cardNumber = cardNumber2;
		this.cardId = cardId2;
		this.cardAccessType = cardAccessType2;
		this.isAddressSaved = isAddressSaved2;
		this.cardType = cardType2;
		this.isGDSCard = isGDSCard2;
		this.bookingType = bookingType2;
		this.cvvLabel = cvvLabel2;
		this.cvvLength= cvvLength2;
		this.bankName= cardBank;
		this.cardHolderFirstName = cardHolderFirstName;
		this.cardHolderLastName = cardHolderLastName;
		this.expiryMonth = expiryMonth;
		this.expiryYear = expiryYear;
		this.tokenized=tokenized;

	
	}

	public QuickBookCard(String cardTypeLabel, String cardBrand, String cardLogoURL, String cardNumber, String cvvLabel, String cvvLength, String cardId,String cardAccessType, boolean isAddressSaved, String cardName, String cardHolderFirstName, String cardHolderLastName, String expiryMonth, String expiryYear, String cardType, boolean isGDSCard, String bookingType,boolean tokenized){
		this.cardTypeLabel = cardTypeLabel;
		this.cardBrand = cardBrand;
		this.cardLogoURL = cardLogoURL;
		this.cardNumber = cardNumber;
		this.cvvLabel = cvvLabel;
		this.cvvLength = cvvLength;
		this.cardId = cardId;
		this.cardAccessType = cardAccessType;
		this.isAddressSaved = isAddressSaved;
		this.cardName = cardName;
		this.cardHolderFirstName = cardHolderFirstName;
		this.cardHolderLastName = cardHolderLastName;
		this.expiryMonth = expiryMonth;
		this.cardType = cardType;
		this.expiryYear = expiryYear;
		this.isGDSCard = isGDSCard;
		this.bookingType = bookingType;
		this.tokenized=tokenized;
	}
	
	public String getCardTypeLabel() {
		return cardTypeLabel;
	}

	public String getCardBrand() {
		return cardBrand;
	}

	public String getCardLogoURL() {
		return cardLogoURL;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public String getCvvLabel() {
		return cvvLabel;
	}

	public String getCvvLength() {
		return cvvLength;
	}

	public String getCardId() {
		return cardId;
	}

	public String getCardAccessType() {
		return cardAccessType;
	}

	public boolean isAddressSaved() {
		return isAddressSaved;
	}

	public String getCardName() {
		return cardName;
	}

	public String getCardHolderFirstName() {
		return cardHolderFirstName;
	}

	public String getCardHolderLastName() {
		return cardHolderLastName;
	}
	

	public String getExpiryMonth() {
		return expiryMonth;
	}

	public String getExpiryYear() {
		return expiryYear;
	}

	public String getCardType() {
		return cardType;
	}

	public boolean isIsGDSCard() {
		return isGDSCard;
	}

	public String getBookingType() {
		return bookingType;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	//Todo remove after testing

	public boolean isTokenized() {
		return tokenized;
	}

	public void setTokenized(boolean tokenized) {
		this.tokenized = tokenized;
	}

	//FOR TESTING PURPOSE
//	@Override
//	public String toString() {
//		return "QuickBookCard{" +
//				"cardId='" + cardId + '\'' +
//				", cardTypeLabel='" + cardTypeLabel + '\'' +
//				", cardBrand='" + cardBrand + '\'' +
//				", cardLogoURL='" + cardLogoURL + '\'' +
//				", cardNumber='" + cardNumber + '\'' +
//				", cvvLabel='" + cvvLabel + '\'' +
//				", cvvLength='" + cvvLength + '\'' +
//				", cardAccessType='" + cardAccessType + '\'' +
//				", isAddressSaved=" + isAddressSaved +
//				", cardName='" + cardName + '\'' +
//				", cardHolderFirstName='" + cardHolderFirstName + '\'' +
//				", cardHolderLastName='" + cardHolderLastName + '\'' +
//				", expiryMonth='" + expiryMonth + '\'' +
//				", expiryYear='" + expiryYear + '\'' +
//				", cardType='" + cardType + '\'' +
//				", isGDSCard=" + isGDSCard +
//				", bookingType='" + bookingType + '\'' +
//				", bankName='" + bankName + '\'' +
//				", tokenRefID='" + tokenRefID + '\'' +
//				", tokenBin='" + tokenBin + '\'' +
//				", tokenRefNo='" + tokenRefNo + '\'' +
//				", tokenized=" + tokenized +
//				'}';
//	}
}