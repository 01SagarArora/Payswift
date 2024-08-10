package com.yatra.payment.ui.display.beans;

import java.util.List;

public class DebitCardPinPaymentOption extends PaymentOption{
    private List<Bank> banks;
    private String cardHolderNameLabel;
    private String cardNoLabel;
    private List<CardType> cardTypes;
    private BillingAddress billingAddress;

    public DebitCardPinPaymentOption(PaymentOption paymentOption) {
        super(paymentOption);
        if(paymentOption instanceof DebitCardPinPaymentOption) {
            DebitCardPinPaymentOption debitCardPinPaymentOption = (DebitCardPinPaymentOption) paymentOption;
            this.banks = debitCardPinPaymentOption.getBanks();
            this.cardHolderNameLabel = debitCardPinPaymentOption.getCardHolderNameLabel();
            this.cardNoLabel = debitCardPinPaymentOption.getCardNoLabel();
            this.cardTypes = debitCardPinPaymentOption.getCardTypes();
            this.billingAddress = debitCardPinPaymentOption.getBillingAddress();
        }
    }

    public List<Bank> getBanks() {
        return banks;
    }

    public void setBanks(List<Bank> banks) {
        this.banks = banks;
    }

    public String getCardHolderNameLabel() {
        return cardHolderNameLabel;
    }

    public void setCardHolderNameLabel(String cardHolderNameLabel) {
        this.cardHolderNameLabel = cardHolderNameLabel;
    }

    public String getCardNoLabel() {
        return cardNoLabel;
    }

    public void setCardNoLabel(String cardNoLabel) {
        this.cardNoLabel = cardNoLabel;
    }

    public List<CardType> getCardTypes() {
        return cardTypes;
    }

    public void setCardTypes(List<CardType> cardTypes) {
        this.cardTypes = cardTypes;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }
}
