package com.yatra.payment.ui.service;

import org.json.JSONObject;

public interface MiniServiceI {

	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON);
	public JSONObject validateMandatoryParameters(JSONObject requestJson);
	public String getResultKey();
}
