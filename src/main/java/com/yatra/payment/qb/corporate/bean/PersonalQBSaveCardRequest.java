package com.yatra.payment.qb.corporate.bean;

import com.yatra.express.cards.v3.beans.UserCardInfo;

public class PersonalQBSaveCardRequest extends SaveCardRequest{

    private UserCardInfo userCardInfo;
    private Long tenantId ;
    private String merchant;
    private String product;

    private Long ssoUserId;

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public UserCardInfo getUserCardInfo() {
        return userCardInfo;
    }

    public void setUserCardInfo(UserCardInfo userCardInfo) {
        this.userCardInfo = userCardInfo;
    }

    public Long getSsoUserId() {
        return ssoUserId;
    }

    public void setSsoUserId(Long ssoUserId) {
        this.ssoUserId = ssoUserId;
    }
}