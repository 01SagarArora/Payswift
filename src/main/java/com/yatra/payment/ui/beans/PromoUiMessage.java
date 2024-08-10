package com.yatra.payment.ui.beans;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class PromoUiMessage {
    String promoCode;
    String payOp;
    String message;

    public PromoUiMessage() {
    }

    public PromoUiMessage(String promoCode, String payOp, String message) {
        this.promoCode = promoCode;
        this.payOp = payOp;
        this.message = message;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPayOp() {
        return payOp;
    }

    public void setPayOp(String payOp) {
        this.payOp = payOp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
