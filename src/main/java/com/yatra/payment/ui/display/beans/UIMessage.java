package com.yatra.payment.ui.display.beans;

import com.yatra.payment.ui.enums.MessageType;
import com.yatra.payment.ui.enums.Status;

public class UIMessage {

	private String messageText;
	private MessageType messageType;
	private Status status;

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "UIMessage [messageText=" + messageText + ", messageType=" + messageType + ", status=" + status + "]";
	}
}
