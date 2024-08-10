package com.yatra.payment.payswift.beans;

import java.io.Serializable;
import java.util.HashMap;

import com.yatra.payment.ui.util.CardsUtil;

public class SessionStorageCardInfo implements Serializable {

	private Long userId;
	private String name;
	private String cardType;
	private String cardBrand;
	private String cardNumber;
	private String expiryMonth;
	private String expiryYear;
	private String cvv;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getCardBrand() {
		return cardBrand;
	}
	public void setCardBrand(String cardBrand) {
		this.cardBrand = cardBrand;
	}
	public String getCardNumber() {
		try{
			return CardsUtil.getDecryptedData(cardNumber);
		}
		catch(Exception e) {
			return cardNumber;
		}
	}
	public void setCardNumber(String cardNumber) {
		try {
			this.cardNumber = CardsUtil.getEncryptedData(cardNumber);
		}
		catch(Exception e) {
			this.cardNumber = cardNumber;
		}
	}
	public String getExpiryMonth() {
		try {
			return CardsUtil.getDecryptedData(expiryMonth);
		}
		catch(Exception e) {
			return expiryMonth;
		}
	}
	public void setExpiryMonth(String expiryMonth) {
		try {
			this.expiryMonth = CardsUtil.getEncryptedData(expiryMonth);
		}
		catch(Exception e) {
			this.expiryMonth = expiryMonth;
		}
	}
	public String getExpiryYear() {
		try {
			return CardsUtil.getDecryptedData(expiryYear);
		}
		catch(Exception e) {
			return expiryYear;
		}
	}
	public void setExpiryYear(String expiryYear) {
		try {
			this.expiryYear = CardsUtil.getEncryptedData(expiryYear);
		}
		catch(Exception e) {
			this.expiryYear = expiryYear;
		}
	}
	public String getCvv() {
		try {
			return CardsUtil.getDecryptedData(cvv);
		}
		catch(Exception e) {
			return cvv;
		}
	}
	public void setCvv(String cvv) {
		try {
			this.cvv = CardsUtil.getEncryptedData(cvv);
		}
		catch(Exception e) {
			this.cvv = cvv;
		}
	}
	
}
