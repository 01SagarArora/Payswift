package com.yatra.payment.client.beans;


public class LoginInfoDo {

    private Long userId;
    private String emailId;
    private String userType;
    private String loginMode;
    private Boolean isAuthorized;
    private Boolean status;
    private String httpCode;
    private String loginStatus;
    private String code;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getEmailId() {
        return emailId;
    }
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
    public String getUserType() {
        return userType;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public String getLoginMode() {
        return loginMode;
    }
    public void setLoginMode(String loginMode) {
        this.loginMode = loginMode;
    }
    public Boolean getIsAuthorized() {
        return isAuthorized;
    }
    public void setIsAuthorized(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }
    public Boolean getStatus() {
        return status;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
    public String getHttpCode() {
        return httpCode;
    }
    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }
    public String getLoginStatus() {
        return loginStatus;
    }
    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
}

