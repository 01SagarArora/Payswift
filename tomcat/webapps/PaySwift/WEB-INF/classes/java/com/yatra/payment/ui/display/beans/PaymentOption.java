package com.yatra.payment.ui.display.beans;

import java.util.List;

import com.yatra.payment.ui.enums.Status;

public class PaymentOption {
	
	private String displayText;
	private String code;
	private Status status;
	private Integer priority;
	private List<UIMessage> messages;
	private String iframeEnabled;
	private boolean nonMoto;
	private String paymentOptionMessage;
	
	public PaymentOption() {}
	
	public PaymentOption(PaymentOption paymentOption) {
		this.displayText = paymentOption.getDisplayText();
		this.code = paymentOption.getCode();
		this.status = paymentOption.getStatus();
		this.priority = paymentOption.getPriority();
		this.messages = paymentOption.getMessages();
		this.iframeEnabled = paymentOption.getIframeEnabled();
		this.nonMoto = paymentOption.isNonMoto();
		this.paymentOptionMessage = paymentOption.getPaymentOptionMessage();
	}

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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public List<UIMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<UIMessage> messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		return "PaymentOption [displayText=" + displayText + ", code=" + code + ", status=" + status + ", priority=" + priority + ", messages=" + messages + "]";
	}

	public String getIframeEnabled() {
		return iframeEnabled;
	}

	public void setIframeEnabled(String iframeEnabled) {
		this.iframeEnabled = iframeEnabled;
	}

	public boolean isNonMoto() {
		return nonMoto;
	}

	public void setNonMoto(boolean nonMoto) {
		this.nonMoto = nonMoto;
	}


	public String getPaymentOptionMessage() {
		return paymentOptionMessage;
	}

	public void setPaymentOptionMessage(String paymentOptionMessage) {
		this.paymentOptionMessage = paymentOptionMessage;
	}
}
