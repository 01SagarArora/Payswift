package com.yatra.payment.ui.payswift.service;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIBankDownMessageMapping;
import com.yatra.payment.ui.dao.bean.UIPayopMessageMapping;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;

@Service
public class RuntimeDBLookUpService {

	@Autowired 
	private PayswiftStagesDAOImpl paymentDAO;
	@Autowired PaymentUIGenericDAO<UIPayopMessageMapping> payopMessageMappingDao;
	@Autowired PaymentUIGenericDAO<UIBankDownMessageMapping> bankDownMessageMappingDao;

	public JSONObject getBankDownMessage() {

		JSONObject bankMessageJson = new JSONObject();	
		try {
			List<UIBankDownMessageMapping> bankDownMessageMapping = bankDownMessageMappingDao.getAll();
			if (bankDownMessageMapping != null) {
				for (UIBankDownMessageMapping message : bankDownMessageMapping) {
					String status = message.getMessageStatus();
					if(status.equalsIgnoreCase("ENABLED")) {
						JSONObject innerJson = new JSONObject();
						innerJson.put("message", message.getMessage());
						innerJson.put("PayNow", message.getPaymentStatus());
						bankMessageJson.put(message.getCode(), innerJson);
					}
				}
			}
		}
		catch(Exception e) {

		}
		return bankMessageJson;
	}

	public JSONObject getPayOpMessage() {
		JSONObject payopMessageJson = new JSONObject();	
		JSONObject innerJson = new JSONObject();
		try {
			List<UIPayopMessageMapping> payopMessageMapping = payopMessageMappingDao.getAll();
			if (payopMessageMapping != null) {
				for (UIPayopMessageMapping message : payopMessageMapping) {
					String status = message.getStatus();
					if(status.equalsIgnoreCase("ENABLED")) {
						if(payopMessageJson.length()!=0 && payopMessageJson.has(message.getPaymentOption()))
							innerJson = payopMessageJson.getJSONObject(message.getPaymentOption());
						else
							innerJson = new JSONObject();
						innerJson.put(message.getMessageType(), message.getMessage());
						payopMessageJson.put(message.getPaymentOption(), innerJson);
					}
				}
			}
		}
		catch(Exception e) {

		}
		return payopMessageJson;
	}

	public JSONObject getUIMessage() {
		JSONObject uiMessageJson = new JSONObject();
		try {
			uiMessageJson.put("PayOpMessage", getPayOpMessage());
			uiMessageJson.put("bankMessage", getBankDownMessage());
		}
		catch(Exception e) {
		}
		return uiMessageJson;
	}
}

