package com.yatra.payment.ui.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.BinDetailsDAO;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;

import net.sf.json.JSONObject;

@Component
public class QuickBookUIHelper {

    private static Logger logger = Logger.getLogger(QuickBookUIHelper.class);
    private List<String> officialBins;

    @Autowired
    private BinDetailsDAO binDetailsDao;
    @Autowired
    private PaymentUIHelper paymentUIHelper;

    public boolean displayQBCardsCRP(JSONObject agentPaymentOptionJSON, String bookingType, String entity, String product, boolean passthrough) {
        boolean diplayQBCardsFlag = false;
        try {
            JSONObject payOps = paymentUIHelper.crpPaymentOptionsByBookingType(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
            if (payOps.has("CC")) {
                JSONObject ccPayOPJsonObject = payOps.getJSONObject("CC");
                if (ccPayOPJsonObject.has("showStoredCards")) {
                    diplayQBCardsFlag = ccPayOPJsonObject.getBoolean("showStoredCards");
                }
            } else if (payOps.has("DC")) {
                JSONObject ccPayOPJsonObject = payOps.getJSONObject("DC");
                if (ccPayOPJsonObject.has("showStoredCards")) {
                    diplayQBCardsFlag = ccPayOPJsonObject.getBoolean("showStoredCards");
                }
            }
        } catch (Exception ex) {
            logger.error("Error while parsing agentPaymentOptionJSON to get showStoredCards Flag, Exception is : " + ex);
        }
        return diplayQBCardsFlag;
    }

    public void filerCardsBasedOnConfig(QuickBookPaymentOption exisitingCards, String cardTypeAccessConfigJson, String bookingType, String configProduct) {
        try {

            if (exisitingCards == null || exisitingCards.getQuickBookCards() == null) {
                logger.error("There are no cards saved by user");
                return;
            }

            List<QuickBookCard> filteredCardList = new ArrayList<>();
            List<QuickBookCard> personalCards = new ArrayList<>();
            List<QuickBookCard> officialCards = new ArrayList<>();
            for (QuickBookCard card : exisitingCards.getQuickBookCards()) {
                if (isCorpBin(card.getCardNumber().replaceAll("-", "").substring(0, 9))) {
                    officialCards.add(card);
                } else {
                    personalCards.add(card);
                }


            }

            JSONObject cardTypeAccessConfigJsonObj = JSONObject.fromObject(cardTypeAccessConfigJson);

            if (!cardTypeAccessConfigJson.contains("success") || !cardTypeAccessConfigJsonObj.getBoolean("success")) {
                logger.debug("Failure respopnse from the config service, showing only " + bookingType + " cards");
                filteredCardList.addAll(officialCards);
                filteredCardList.addAll(personalCards);
                exisitingCards.setQuickBookCards(filteredCardList);
                return;
            }

            JSONObject configJson = cardTypeAccessConfigJsonObj.getJSONObject("configurations")
                    .getJSONObject("cardTypeAccessConfig")
                    .getJSONObject(configProduct)
                    .getJSONObject(bookingType.toLowerCase());

            int officialCardsPriority = configJson.getInt("officialCardTypePriority");
            int personalCardsPriority = configJson.getInt("personalCardTypePriority");

            if (officialCardsPriority > 1 || personalCardsPriority > 1) {
                if (officialCardsPriority > personalCardsPriority) {
                    if (personalCards.size() > 0) {
                        filteredCardList.addAll(personalCards);
                        exisitingCards.setQuickBookCards(filteredCardList);
                        return;
                    } else {
                        filteredCardList.addAll(officialCards);
                        exisitingCards.setQuickBookCards(filteredCardList);
                        return;
                    }
                } else {
                    if (officialCards.size() > 0) {
                        filteredCardList.addAll(officialCards);
                        exisitingCards.setQuickBookCards(filteredCardList);
                        return;
                    } else {
                        filteredCardList.addAll(personalCards);
                        exisitingCards.setQuickBookCards(filteredCardList);
                        return;
                    }
                }
            } else {
                if (officialCardsPriority == 1 && officialCards.size() > 0) {
                    filteredCardList.addAll(officialCards);
                    exisitingCards.setQuickBookCards(filteredCardList);
                }

                if (personalCardsPriority == 1 && personalCards.size() > 0) {
                    filteredCardList.addAll(personalCards);
                    exisitingCards.setQuickBookCards(filteredCardList);
                }
            }

        } catch (Exception ex) {
            logger.error("Exception while filtering cards by Booking Type, returning all cards. Exception is :", ex);
        }
    }

    private List<String> getCorpBins() {
        if (officialBins == null || officialBins.size() < 1) {
            officialBins = binDetailsDao.getOfficialCorporateBins();
            return officialBins;
        } else {
            return officialBins;
        }
    }

    private boolean isCorpBin(String bin) {
        return binDetailsDao.isOfficialCorporateBin(bin);
    }

}
