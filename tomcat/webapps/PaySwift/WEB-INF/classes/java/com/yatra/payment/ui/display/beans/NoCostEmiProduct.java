package com.yatra.payment.ui.display.beans;

import java.util.List;


public class NoCostEmiProduct {
	
    private List<NoCostEmiBank> emiBanks;
    private Boolean isPromoAllowed;
    
    public NoCostEmiProduct() {}

    public NoCostEmiProduct(List<NoCostEmiBank> emiBanks) {
        this.emiBanks = emiBanks;
    }

    public List<NoCostEmiBank> getEmiBanks() {
        return emiBanks;
    }

    public void setEmiBanks(List<NoCostEmiBank> emiBanks) {
        this.emiBanks = emiBanks;
    }

    public Boolean getIsPromoAllowed() {
        return isPromoAllowed;
    }

    public void setIsPromoAllowed(Boolean isPromoAllowed) {
        this.isPromoAllowed = isPromoAllowed;
    }
	
}
