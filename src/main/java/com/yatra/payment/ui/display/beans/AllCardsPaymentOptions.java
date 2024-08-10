package com.yatra.payment.ui.display.beans;

import java.util.List;

public class AllCardsPaymentOptions extends  PaymentOption{

    List <PaymentOption> cardList;

    public void setCardList(List<PaymentOption> cardList) {
        this.cardList = cardList;
    }

    public List<PaymentOption> getCardList() {
        return cardList;
    }




}
