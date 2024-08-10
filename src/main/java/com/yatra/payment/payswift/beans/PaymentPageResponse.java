package com.yatra.payment.payswift.beans;

import com.yatra.payment.ui.display.beans.PaymentDiv;

import java.util.HashMap;
import java.util.Map;

public class PaymentPageResponse {
	
	private PaymentDiv paymentDiv;
	private String lobDetailsJson;
	private Map<String, Object> additionalParams = new HashMap<>();
	private String corpAgentProfileType;

	public PaymentDiv getPaymentDiv() {
		return paymentDiv;
	}
	public void setPaymentDiv(PaymentDiv paymentDiv) {
		this.paymentDiv = paymentDiv;
	}
	public String getLobDetailsJson() {
		return lobDetailsJson;
	}
	public void setLobDetailsJson(String lobDetailsJson) {
		this.lobDetailsJson = lobDetailsJson;
	}

	public void addParam(String key, Object value) {
		additionalParams.put(key, value);
	}

	public Map<String, Object> getAdditionalParams() {
		return additionalParams;
	}
	public String getCorpAgentProfileType() {
		return corpAgentProfileType;
	}
	public void setCorpAgentProfileType(String corpAgentProfileType) {
		this.corpAgentProfileType = corpAgentProfileType;
	}
}
