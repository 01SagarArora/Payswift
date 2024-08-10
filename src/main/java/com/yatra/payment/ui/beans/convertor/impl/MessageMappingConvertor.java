package com.yatra.payment.ui.beans.convertor.impl;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIPayopMessageMapping;
import com.yatra.payment.ui.display.beans.UIMessage;
import com.yatra.payment.ui.enums.MessageType;
import com.yatra.payment.ui.enums.Status;

@Service("messageMappingConvertor")
public class MessageMappingConvertor implements BeanConvertor<UIPayopMessageMapping, UIMessage> {

	@Override
	public UIMessage convert(UIPayopMessageMapping input) {
		UIMessage uiMessage = new UIMessage();
		uiMessage.setMessageText(input.getMessage());
		uiMessage.setMessageType(MessageType.valueOf(input.getMessageType()));
		uiMessage.setStatus(Status.valueOf(input.getStatus()));
		return uiMessage;
		//return null;
	}

}
