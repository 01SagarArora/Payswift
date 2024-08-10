package com.yatra.payment.payswift.beans;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import net.sf.json.JSONObject;

import java.util.Optional;

import static com.yatra.payment.ui.enums.FailureCode.NO_FAILURE_CAPTURED;
import net.sf.json.JSONNull;

public class B2BUserProfileDetailBean {
    private JSONObject response;
    private APIStatusBean statusBean;

    public B2BUserProfileDetailBean(JSONObject response, boolean validate) {
        this.response = response;
        this.statusBean = new APIStatusBean();
        if (validate) {
            validateResponse();
        }
    }

    public APIStatusBean getStatusBean() {
        return statusBean;
    }

    public Optional<String> getErpCode(String userProfileType) {
        if (this.statusBean.getResponseStatus() == ResponseStatus.SUCCESS) {
            JSONObject moDetailObj = this.response.getJSONObject("moDetail");
            if (moDetailObj.containsKey(userProfileType)) {
                JSONObject profileDataObj = moDetailObj.getJSONObject(userProfileType);
                return Optional.ofNullable(profileDataObj.getString("erpCode"));
            }
        }
        return Optional.empty();
    }
    
    public String getCorpUserRole() {
        if(!response.has("role")) {
            return "";
        }
        Object role = response.get("role");
        if (role instanceof JSONNull) {
            return "";
        }
        return String.valueOf(role);
    }
    
    public String getClientId() {
        if(!response.has("clientId")) {
            return "";
        }
        Object clientId = response.get("clientId");
        if (clientId instanceof JSONNull) {
            return "";
        }
        return String.valueOf(clientId);
    }
    
    public String getUserId() {
        if(!response.has("userId")) {
            return "";
        }
        Object userId = response.get("userId");
        if (userId instanceof JSONNull) {
            return "";
        }
        return String.valueOf(userId);
    }
    
    public String getUserRole() {
        if(response.has("userDetail") && response.getJSONObject("userDetail").has("userRole"))
        	return response.getJSONObject("userDetail").getString("userRole");
        return "";
    }
    
    private void validateResponse() {
        if (this.statusBean.getResponseStatus() == ResponseStatus.FAILURE) {
            return;
        }

        if (this.response.getInt("resCode") != 200) {
            this.statusBean.setFailureCode(FailureCode.B2B_USER_PROFILE_SERVICE_FAIL_RESPONSE);
        }

        FailureCode responseFailureCode = this.statusBean.getFailureCode();
        if (responseFailureCode != NO_FAILURE_CAPTURED) {
            throw new PayswiftException(responseFailureCode.toString(), responseFailureCode.getCode());
        }
    }
}
