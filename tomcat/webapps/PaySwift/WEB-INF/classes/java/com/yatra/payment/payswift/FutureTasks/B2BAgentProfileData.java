package com.yatra.payment.payswift.FutureTasks;

import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import com.yatra.payment.ui.service.AgentProfileService;

public class B2BAgentProfileData implements Callable {

    private AgentProfileService agentProfileService;
    private String ssoToken;
    private boolean getCreditPoolDetails;
    private String productCode;
    private String ctripId;
    
    public B2BAgentProfileData(AgentProfileService agentProfileService, String ssoToken, boolean getCreditPoolDetails, String productCode, String ctripId) {
        this.agentProfileService = agentProfileService;
        this.ssoToken = ssoToken;
        this.getCreditPoolDetails = getCreditPoolDetails;
        this.productCode = productCode;
        this.ctripId = ctripId;
    }

    @Override
    public JSONObject call() throws Exception {
        if (getCreditPoolDetails) {
            return agentProfileService.getCreditPoolDetails(ssoToken, productCode, ctripId);
        }
        return agentProfileService.getAgentPaymentOptionsAsJsonObject(ssoToken, productCode);
    }

}
