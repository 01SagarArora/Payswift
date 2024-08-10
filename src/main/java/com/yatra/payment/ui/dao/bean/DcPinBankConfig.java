package com.yatra.payment.ui.dao.bean;

public class DcPinBankConfig {
    private String bank_code;
    private String gateway_code;
    private Boolean cardfields_show;

    public String getBank_code() {
        return bank_code;
    }

    public void setBank_code(String bank_code) {
        this.bank_code = bank_code;
    }

    public String getGateway_code() {
        return gateway_code;
    }

    public void setGateway_code(String gateway_code) {
        this.gateway_code = gateway_code;
    }

    public Boolean getCardfields_show() {
        return cardfields_show;
    }

    public void setCardfields_show(Boolean cardfields_show) {
        this.cardfields_show = cardfields_show;
    }
}
