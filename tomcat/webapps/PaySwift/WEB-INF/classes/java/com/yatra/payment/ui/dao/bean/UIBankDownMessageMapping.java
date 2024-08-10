package com.yatra.payment.ui.dao.bean;

public class UIBankDownMessageMapping {
	
	String name;
	String code;
	String message;
	String message_status;
	String payment_status;
	
	
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageStatus() {
		return message_status;
	}
	public void setMessageStatus(String message_status) {
		this.message_status = message_status;
	}
	public String getPaymentStatus() {
		return payment_status;
	}
	public void setPaymentStatus(String payment_status) {
		this.payment_status = payment_status;
	}


}
