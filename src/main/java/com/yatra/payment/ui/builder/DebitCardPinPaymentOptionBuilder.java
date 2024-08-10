package com.yatra.payment.ui.builder;


import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.display.beans.*;
import com.yatra.payment.ui.service.StatusProviderService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("debitCardPinPaymentOptionBuilder")
public class DebitCardPinPaymentOptionBuilder extends CardOptionBuilder{

    private static Logger logger = Logger.getLogger(DebitCardPinPaymentOptionBuilder.class);
    @Autowired
    private UICacheManager<PaymentOption> paymentOptionCache;
    @Autowired	private UICacheManager<Bank> debitCardPinBankCache;
    @Autowired protected StatusProviderService statusProviderService;

    @Override
    public PaymentOption build(PaymentOption paymentOption) throws Exception {
        try {
            DebitCardPinPaymentOption debitCardPinPaymentOption= new DebitCardPinPaymentOption(paymentOption);
            debitCardPinPaymentOption.setCardHolderNameLabel(getUIParamMappingValue(COMMON_CONSTANT, COMMON_CONSTANT, "cardHolderNameLabel"));
            debitCardPinPaymentOption.setCardNoLabel(getUIParamMappingValue(paymentOption.getCode(), COMMON_CONSTANT, "cardNoLabel"));
            debitCardPinPaymentOption.setBillingAddress(getUIBillingAddress());
            return debitCardPinPaymentOption;

        } catch (Exception e) {
            logger.error("SEVERE!! Error occurred while building Debit Card Pin Payment Option ", e);
            throw e;
        }
    }

    @Override
    public PaymentOption build(String merchantCode, String productCode) throws Exception {
        logger.info("Getting Debit Card Pin Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);

        Boolean isPayOpEnabled = statusProviderService.isPaymentOptionEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_DEBIT_PIN);
        logger.info("Is debit card pin enabled for merchant : " + merchantCode + " and product : " + productCode + " : " + isPayOpEnabled);

        if(isPayOpEnabled == null) {
            logger.info("Could not be determined if debit card pin enabled for merchant : " + merchantCode + " and product : " + productCode + ". Returning null..");
            return null;
        }
        PaymentOption commonPaymentOption = paymentOptionCache.get(PaymentUIUtil.PAYMENT_OPTION_DEBIT_PIN);

        if(commonPaymentOption == null) {
            logger.info("No common Debit Card Pin payment option bean exists. Returning null..");
            return null;
        }

        DebitCardPinPaymentOption debitCardPinPaymentOption = new DebitCardPinPaymentOption(commonPaymentOption);
        //all debit card pin banks are enabled for any merchant and product combination, the disabling for any merchant-product combination can be done later if need arises
        List<Bank> bankList = getEnabledBanks();
        debitCardPinPaymentOption.setBanks(bankList);
        List<CardType> cardTypeList = getEnabledCardTypes(debitCardPinPaymentOption.getCode(), merchantCode, productCode);
        debitCardPinPaymentOption.setCardTypes(cardTypeList);
        debitCardPinPaymentOption.setStatus(PaymentUIUtil.getStatus(isPayOpEnabled));
        logger.info("Returning Debit Card Pin Payment Option bean for merchant : " + merchantCode + " and product : " + productCode);
        return debitCardPinPaymentOption;
    }

    private List<Bank> getEnabledBanks() throws Exception {

        List<Bank> productBankList = new ArrayList<Bank>();
        try {
            List<Bank> bankList = debitCardPinBankCache.getAll();
            if(bankList == null || bankList.isEmpty()) {
                logger.error("No banks obtained from DebitCardPinBankCache. Returning an empty list.");
                return productBankList;
            }
            for(Bank bank : bankList) {
                String bankCode = bank.getCode();

                boolean isBankEnabled = PaymentUIUtil.isEnabled(bank.getStatus());
                logger.info("Is bank : " + bankCode + " enabled : " + isBankEnabled);

                if(isBankEnabled) {
                    productBankList.add(bank);
                }
            }
            BankComparator bankComparator = new BankComparator();
            Collections.sort(productBankList, bankComparator);
            return productBankList;

        } catch (Exception e) {
            logger.info("Exception ocurred while fetching enabled debit card pin banks", e);
            throw e;
        }
    }
}
