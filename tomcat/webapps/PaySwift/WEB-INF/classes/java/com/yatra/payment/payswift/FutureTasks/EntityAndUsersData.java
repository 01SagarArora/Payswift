package com.yatra.payment.payswift.FutureTasks;

import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.payment.ui.service.impl.CorporateProfileServiceImpl;
import net.sf.json.JSONArray;

public class EntityAndUsersData implements Callable {

    private CorporateProfileServiceImpl corporateProfileService;
    private JSONArray requestData;
    private boolean isUsersDataRequired;
    
    public EntityAndUsersData(CorporateProfileServiceImpl corporateProfileService, JSONArray requestData, boolean isUsersDataRequired) {
        this.corporateProfileService = corporateProfileService;
        this.requestData = requestData;
        this.isUsersDataRequired = isUsersDataRequired;
    }

    @Override
    public String call() throws Exception {
        if (isUsersDataRequired) {
            return corporateProfileService.getUsersData(requestData);
        }
        return corporateProfileService.getEntityData(requestData);
    }

}
