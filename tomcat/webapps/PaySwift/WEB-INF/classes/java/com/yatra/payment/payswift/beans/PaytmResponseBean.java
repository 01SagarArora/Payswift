package com.yatra.payment.payswift.beans;


public class PaytmResponseBean {

    private String resultStatus;

    private String resultCode;

    private String resultMsg;

    public PaytmResponseBean(String resultStatus, String resultCode, String resultMsg) {
        this.resultStatus = resultStatus;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }


    public PaytmResponseBean() {
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    @Override
    public String toString() {
        return "PaytmResponseBean{" +
                "resultStatus='" + resultStatus + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                '}';
    }
}
