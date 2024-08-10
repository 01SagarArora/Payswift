package com.yatra.payment.ui.display.beans;

import java.util.List;

public class AllUpiPaymentOption extends PaymentOption{
    List<PaymentOption> upiList;
    private List<String> upiBankCodes;

    public List<PaymentOption> getUpiList() {
        return upiList;
    }

    public void setUpiList(List<PaymentOption> upiList) {
        this.upiList = upiList;
    }

    public List<String> getUpiBankCodes() {
        return upiBankCodes;
    }

    public void setUpiBankCodes(List<String> upiBankCodes) {
        this.upiBankCodes = upiBankCodes;
    }
}
