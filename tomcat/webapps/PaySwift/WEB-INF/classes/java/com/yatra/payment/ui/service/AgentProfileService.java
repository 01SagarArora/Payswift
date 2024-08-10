package com.yatra.payment.ui.service;

import java.util.List;

import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;

import net.sf.json.JSONObject;

public interface AgentProfileService {

    public boolean isCrpProduct(String productCode);
	public List<String> getAgentPaymentOptions(String ssoToken) throws Exception;
    public JSONObject getCreditPoolDetails(String ssoToken, String productCode, String ctripId);
    public JSONObject getUserDetailsCorp(String ssoToken);
    public JSONObject getCreditPoolCorp(JSONObject userDetails, String ssoToken, String ctripId);
    public JSONObject getAgentPaymentOptionsAsJsonObject(String ssoToken,String ProductCode) throws Exception;
    JSONObject agentLogin(String emailId, String ssoToken);
    public B2BUserProfileDetailBean getUserProfileDetail(String ssoToken);
    public B2BUserProfileDetailBean getUserProfileDetailCorp(String ssoToken, String merchantCode, String productCode);
    public String getAclDetailsForYatraCards(String accessToken);
}
