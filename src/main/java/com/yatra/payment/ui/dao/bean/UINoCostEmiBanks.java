package com.yatra.payment.ui.dao.bean;

import java.util.List;

public class UINoCostEmiBanks {

    private String lob;
    private List<String> products;
    private String bankConfigurations;
    private Integer isPromoAllowed = 0;

    public String getLob() {
        return lob;
    }

    public void setLob(String lob) {
        this.lob = lob;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }
    
    public String getBankConfigurations() {
        return bankConfigurations;
    }

    public void setBankConfigurations(String bankConfigurations) {
        this.bankConfigurations = bankConfigurations;
    }

    public Integer getIsPromoAllowed() {
        return isPromoAllowed;
    }

    public void setIsPromoAllowed(Integer isPromoAllowed) {
        this.isPromoAllowed = isPromoAllowed;
    }


}
