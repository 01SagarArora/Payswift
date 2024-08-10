/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.offline.beans;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
public class GDSCardInfo {
    
    private String cardBrandCode;
    private String cardNumber;
    private String expiryYear;
    private String expiryMonth;

    public String getCardBrandCode() {
        return cardBrandCode;
    }

    public void setCardBrandCode(String cardBrandCode) {
        this.cardBrandCode = cardBrandCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    @Override
    public String toString() {
        return cardBrandCode + "" + cardNumber + "/D" + expiryMonth + "" + expiryYear;
    }
    
    
}
