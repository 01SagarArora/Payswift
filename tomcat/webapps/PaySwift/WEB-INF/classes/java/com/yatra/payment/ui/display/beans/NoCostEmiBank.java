package com.yatra.payment.ui.display.beans;

import java.util.List;


public class NoCostEmiBank {
    
    private String bank;
    private List<EmiType> emiTypes;

    public NoCostEmiBank() {}

    public NoCostEmiBank(List<EmiType> emiTypes) {
        this.emiTypes = emiTypes;
    }

    public List<EmiType> getEmiTypes() {
        return emiTypes;
    }

    public void setEmiTypes(List<EmiType> emiTypes) {
        this.emiTypes = emiTypes;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }
	
	
}
